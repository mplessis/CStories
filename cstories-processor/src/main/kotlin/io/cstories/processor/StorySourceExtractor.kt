package io.cstories.processor

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.File

/**
 * Extracts, from a `@CStory` function's own source file, the exact
 * expressions that invoke its referenced `@CStoryComponent`.
 *
 * This is a lightweight text-based extraction (brace/paren balancing), not
 * a real AST — KSP only exposes symbol-level information, not
 * expression-level source structure. It never looks outside the story's
 * own module/file (unlike documentation resolution), so no cross-module
 * fallback is needed here.
 */
internal object StorySourceExtractor {

    /**
     * Returns the source text of every call to [componentFunctionName]
     * found inside [story]'s body, in source order and separated by a blank
     * line, or `null` if the story's source can't be read or no call can be
     * located (e.g. the component is invoked indirectly, or via an alias).
     */
    fun extractUsageCode(story: KSFunctionDeclaration, componentFunctionName: String): String? {
        val body = extractFunctionBody(story) ?: return null
        return extractCalls(body, componentFunctionName).ifEmpty { null }?.joinToString("\n\n")
    }

    private fun extractFunctionBody(function: KSFunctionDeclaration): String? {
        val location = function.location as? FileLocation ?: return null
        val filePath = function.containingFile?.filePath ?: return null
        val file = File(filePath)
        if (!file.isFile) return null
        val lines = runCatching { file.readLines() }.getOrNull() ?: return null

        // `location.lineNumber` is 1-based and points at the line where the
        // declaration starts (e.g. the `fun` keyword line, or an annotation
        // line right above it — either way, the opening `{` of the body is
        // on or after this line).
        val startIndex = (location.lineNumber - 1).coerceIn(0, lines.lastIndex)
        val fromDeclaration = lines.subList(startIndex, lines.size).joinToString("\n")

        val openBrace = fromDeclaration.indexOf('{')
        if (openBrace == -1) return null

        var depth = 0
        var i = openBrace
        while (i < fromDeclaration.length) {
            when (fromDeclaration[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return fromDeclaration.substring(openBrace + 1, i)
                }
            }
            i++
        }
        return null
    }

    /**
     * Returns every top-level call to [functionName] in [body], each one
     * re-indented relative to its own start column. Calls nested inside an
     * already extracted call (e.g. in one of its lambda arguments) are part
     * of that call's text and are not returned again.
     *
     * Exposed at `internal` visibility for direct unit testing of the
     * paren-balancing heuristic, without needing a real KSP symbol/file.
     */
    internal fun extractCalls(body: String, functionName: String): List<String> {
        val calls = mutableListOf<String>()
        var searchFrom = 0
        while (true) {
            val callStart = findCallStart(body, functionName, searchFrom) ?: break
            val callEnd = findCallEnd(body, callStart) ?: break
            calls += dedent(body.substring(callStart, callEnd), columnOf(body, callStart))
            searchFrom = callEnd
        }
        return calls
    }

    /** Returns the index right after the parenthesis closing the call starting at [callStart]. */
    private fun findCallEnd(body: String, callStart: Int): Int? {
        val openParen = body.indexOf('(', callStart)
        if (openParen == -1) return null

        var depth = 0
        var i = openParen
        while (i < body.length) {
            when (body[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i + 1
                }
            }
            i++
        }
        return null
    }

    private fun columnOf(text: String, index: Int): Int = index - (text.lastIndexOf('\n', index - 1) + 1)

    /** Removes up to [column] leading whitespace characters from every line but the first. */
    private fun dedent(call: String, column: Int): String =
        call.lines().mapIndexed { index, line ->
            if (index == 0) {
                line
            } else {
                val removable = line.takeWhile { it == ' ' || it == '\t' }.length.coerceAtMost(column)
                line.substring(removable)
            }
        }.joinToString("\n").trimEnd()

    /** Finds the start of `functionName(`, ensuring it's a whole identifier match (not a substring of a longer name). */
    private fun findCallStart(body: String, functionName: String, from: Int): Int? {
        var searchFrom = from
        while (true) {
            val index = body.indexOf(functionName, searchFrom)
            if (index == -1) return null

            val before = body.getOrNull(index - 1)
            val isWordBoundaryBefore = before == null || !(before.isLetterOrDigit() || before == '_')

            var afterIndex = index + functionName.length
            while (afterIndex < body.length && body[afterIndex] == ' ') afterIndex++
            val isFollowedByParen = body.getOrNull(afterIndex) == '('

            if (isWordBoundaryBefore && isFollowedByParen) return findQualifierStart(body, index)
            searchFrom = index + functionName.length
        }
    }

    /**
     * Walks backward from [nameStart] over any dotted qualifier chain (e.g.
     * `DemoButton.`) so the returned call start includes the receiver, not
     * just the invoked function's own name.
     */
    private fun findQualifierStart(body: String, nameStart: Int): Int {
        var i = nameStart
        while (i > 0 && body[i - 1] == '.') {
            val dotIndex = i - 1
            var k = dotIndex - 1
            while (k >= 0 && (body[k].isLetterOrDigit() || body[k] == '_')) k--
            if (k == dotIndex - 1) break // no identifier before the dot, stop
            i = k + 1
        }
        return i
    }
}
