package io.cstories.processor

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.File

/**
 * Extracts, from a `@CStory` function's own source file, the exact
 * expression that invokes its referenced `@CStoryComponent`.
 *
 * This is a lightweight text-based extraction (brace/paren balancing), not
 * a real AST — KSP only exposes symbol-level information, not
 * expression-level source structure. It never looks outside the story's
 * own module/file (unlike documentation resolution), so no cross-module
 * fallback is needed here.
 */
internal object StorySourceExtractor {

    /**
     * Returns the source text of the call to [componentFunctionName] found
     * inside [story]'s body, or `null` if the story's source can't be read
     * or the call can't be located (e.g. the component is invoked
     * indirectly, or via an alias).
     */
    fun extractUsageCode(story: KSFunctionDeclaration, componentFunctionName: String): String? {
        val body = extractFunctionBody(story) ?: return null
        return extractCall(body, componentFunctionName)
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
                    if (depth == 0) return fromDeclaration.substring(openBrace + 1, i).trim()
                }
            }
            i++
        }
        return null
    }

    /** Exposed at `internal` visibility for direct unit testing of the paren-balancing heuristic, without needing a real KSP symbol/file. */
    internal fun extractCall(body: String, functionName: String): String? {
        val callStart = findCallStart(body, functionName) ?: return null
        val openParen = body.indexOf('(', callStart)
        if (openParen == -1) return null

        var depth = 0
        var i = openParen
        while (i < body.length) {
            when (body[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return body.substring(callStart, i + 1).trim()
                }
            }
            i++
        }
        return null
    }

    /** Finds the start of `functionName(`, ensuring it's a whole identifier match (not a substring of a longer name). */
    private fun findCallStart(body: String, functionName: String): Int? {
        var searchFrom = 0
        while (true) {
            val index = body.indexOf(functionName, searchFrom)
            if (index == -1) return null

            val before = body.getOrNull(index - 1)
            val isWordBoundaryBefore = before == null || !(before.isLetterOrDigit() || before == '_')

            var afterIndex = index + functionName.length
            while (afterIndex < body.length && body[afterIndex] == ' ') afterIndex++
            val isFollowedByParen = body.getOrNull(afterIndex) == '('

            if (isWordBoundaryBefore && isFollowedByParen) return index
            searchFrom = index + functionName.length
        }
    }
}
