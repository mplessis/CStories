package io.cstories.runtime

/**
 * Result of lexing a Kotlin source snippet for syntax highlighting purposes.
 * This is a lightweight hand-rolled lexer (not a real parser) — kept
 * dependency-free to stay Kotlin/Wasm-friendly, following the same
 * philosophy as [MarkdownText]'s Markdown renderer.
 */
internal sealed interface KotlinToken {
    val text: String

    data class Keyword(override val text: String) : KotlinToken
    data class StringLiteral(override val text: String, val parts: List<StringPart>) : KotlinToken
    data class Comment(override val text: String) : KotlinToken
    data class NumberLiteral(override val text: String) : KotlinToken
    data class Annotation(override val text: String) : KotlinToken

    /** An identifier used as a type or invoked as a function/constructor (e.g. `PrimaryButton(`, `List<String>`). */
    data class TypeOrFunctionCall(override val text: String) : KotlinToken

    data class Plain(override val text: String) : KotlinToken
}

/** A chunk of a string literal's content: either literal text or a `$x` / `${expr}` interpolation. */
internal sealed interface StringPart {
    data class Literal(val text: String) : StringPart
    data class Interpolation(val text: String) : StringPart
}

internal object KotlinSyntaxHighlighter {

    private val keywords = setOf(
        "val", "var", "fun", "if", "else", "when", "for", "while", "do", "return",
        "true", "false", "null", "it", "this", "super", "is", "as", "in", "!in", "!is",
        "object", "class", "interface", "companion", "by", "where", "import", "package",
        "typealias", "constructor", "init", "try", "catch", "finally", "throw",
        "break", "continue", "sealed", "data", "enum", "annotation", "override",
        "private", "internal", "public", "protected", "abstract", "open", "final",
        "vararg", "reified", "inline", "noinline", "crossinline", "suspend",
        "operator", "infix", "tailrec", "external", "lateinit", "const", "out", "get", "set",
    )

    fun tokenize(code: String): List<KotlinToken> {
        val tokens = mutableListOf<KotlinToken>()
        val plainBuffer = StringBuilder()
        var i = 0

        fun flushPlain() {
            if (plainBuffer.isNotEmpty()) {
                tokens += KotlinToken.Plain(plainBuffer.toString())
                plainBuffer.clear()
            }
        }

        while (i < code.length) {
            val c = code[i]

            when {
                // Line comment
                c == '/' && code.getOrNull(i + 1) == '/' -> {
                    flushPlain()
                    val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                    tokens += KotlinToken.Comment(code.substring(i, end))
                    i = end
                }

                // Block comment
                c == '/' && code.getOrNull(i + 1) == '*' -> {
                    flushPlain()
                    val end = code.indexOf("*/", i + 2).let { if (it == -1) code.length else it + 2 }
                    tokens += KotlinToken.Comment(code.substring(i, end))
                    i = end
                }

                // String literal (single or triple-quoted)
                c == '"' -> {
                    flushPlain()
                    val triple = code.startsWith("\"\"\"", i)
                    val quote = if (triple) "\"\"\"" else "\""
                    val (literalText, endIndex) = readStringLiteral(code, i, quote)
                    tokens += KotlinToken.StringLiteral(literalText, parseStringParts(literalText, quote))
                    i = endIndex
                }

                // Annotation
                c == '@' && code.getOrNull(i + 1)?.isIdentifierStart() == true -> {
                    flushPlain()
                    var end = i + 1
                    while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_')) end++
                    tokens += KotlinToken.Annotation(code.substring(i, end))
                    i = end
                }

                // Number literal
                c.isDigit() -> {
                    flushPlain()
                    var end = i
                    if (code.startsWith("0x", end, ignoreCase = true) || code.startsWith("0b", end, ignoreCase = true)) {
                        end += 2
                        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_')) end++
                    } else {
                        while (end < code.length && (code[end].isDigit() || code[end] == '_')) end++
                        if (code.getOrNull(end) == '.' && code.getOrNull(end + 1)?.isDigit() == true) {
                            end++
                            while (end < code.length && (code[end].isDigit() || code[end] == '_')) end++
                        }
                        while (end < code.length && code[end] in "fFlLuU") end++
                    }
                    tokens += KotlinToken.NumberLiteral(code.substring(i, end))
                    i = end
                }

                // Identifier (keyword, type/function call, or plain)
                c.isIdentifierStart() -> {
                    flushPlain()
                    var end = i + 1
                    while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_')) end++
                    val word = code.substring(i, end)

                    var lookahead = end
                    while (lookahead < code.length && code[lookahead] == ' ') lookahead++
                    val followedByParenOrAngle = code.getOrNull(lookahead) == '(' || code.getOrNull(lookahead) == '<'

                    when {
                        word in keywords -> tokens += KotlinToken.Keyword(word)
                        followedByParenOrAngle || word.firstOrNull()?.isUpperCase() == true ->
                            tokens += KotlinToken.TypeOrFunctionCall(word)
                        else -> tokens += KotlinToken.Plain(word)
                    }
                    i = end
                }

                else -> {
                    plainBuffer.append(c)
                    i++
                }
            }
        }
        flushPlain()
        return tokens
    }

    /** Reads a (possibly triple-quoted) string literal starting at [start], returning its full text (including quotes) and the index right after it. */
    private fun readStringLiteral(code: String, start: Int, quote: String): Pair<String, Int> {
        var i = start + quote.length
        while (i < code.length) {
            if (quote == "\"" && code[i] == '\\') {
                i += 2
                continue
            }
            if (code.startsWith(quote, i)) {
                i += quote.length
                return code.substring(start, i) to i
            }
            i++
        }
        return code.substring(start) to code.length
    }

    /** Splits a full string literal's text (with surrounding quotes) into literal/interpolation parts. */
    private fun parseStringParts(literalText: String, quote: String): List<StringPart> {
        val inner = literalText.removePrefix(quote).removeSuffix(quote)
        val parts = mutableListOf<StringPart>()
        val buffer = StringBuilder()
        var i = 0

        fun flush() {
            if (buffer.isNotEmpty()) {
                parts += StringPart.Literal(buffer.toString())
                buffer.clear()
            }
        }

        while (i < inner.length) {
            val c = inner[i]
            when {
                c == '\\' && quote == "\"" && i + 1 < inner.length -> {
                    buffer.append(c).append(inner[i + 1])
                    i += 2
                }

                c == '$' && inner.getOrNull(i + 1) == '{' -> {
                    flush()
                    var depth = 1
                    var end = i + 2
                    while (end < inner.length && depth > 0) {
                        when (inner[end]) {
                            '{' -> depth++
                            '}' -> depth--
                        }
                        end++
                    }
                    parts += StringPart.Interpolation(inner.substring(i, end))
                    i = end
                }

                c == '$' && inner.getOrNull(i + 1)?.isIdentifierStart() == true -> {
                    flush()
                    var end = i + 1
                    while (end < inner.length && (inner[end].isLetterOrDigit() || inner[end] == '_')) end++
                    parts += StringPart.Interpolation(inner.substring(i, end))
                    i = end
                }

                else -> {
                    buffer.append(c)
                    i++
                }
            }
        }
        flush()
        return parts
    }
}

/** Multiplatform-safe substitute for the JVM-only `Char.isJavaIdentifierStart()`. */
private fun Char.isIdentifierStart(): Boolean = isLetter() || this == '_'
