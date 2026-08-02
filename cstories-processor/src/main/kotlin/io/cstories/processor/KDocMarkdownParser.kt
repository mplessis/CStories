package io.cstories.processor

/** Result of parsing a raw KDoc `docString` into a documentation model. */
internal data class KDocParseResult(
    val markdown: String,
    val unsupportedTags: List<String>,
)

/**
 * Parses a raw KDoc `docString` (as exposed by KSP's `KSDeclaration.docString`)
 * into a Markdown string suitable for display in the story catalog's
 * documentation panel.
 *
 * Supports the description paragraph(s), `@param`, and `@return`/`@returns`
 * tags. Any other tag (`@see`, `@throws`, `@sample`, `@property`...) is
 * reported back via [KDocParseResult.unsupportedTags] so the caller can emit
 * a KSP warning, but does not appear in the rendered output in this version.
 *
 * [paramTypes] optionally enriches each `@param` entry with the resolved
 * type's possible values (enum entries) or variants (sealed subtypes).
 */
internal object KDocMarkdownParser {
    private val tagRegex = Regex("^@(\\w+)\\s*(.*)$")
    private val kdocLinkRegex = Regex("\\[([^]]+)]")

    fun parse(docString: String?, paramTypes: Map<String, ParamTypeInfo> = emptyMap()): KDocParseResult? {
        if (docString.isNullOrBlank()) return null

        val cleaned = cleanDocString(docString)
        if (cleaned.isBlank()) return null

        val description = StringBuilder()
        val blocks = mutableListOf<Pair<String, StringBuilder>>()
        var current: Pair<String, StringBuilder>? = null

        cleaned.lines().forEach { rawLine ->
            val match = tagRegex.find(rawLine.trim())
            if (match != null) {
                val block = match.groupValues[1] to StringBuilder(match.groupValues[2])
                blocks += block
                current = block
            } else {
                val active = current
                if (active != null) {
                    if (rawLine.isNotBlank()) {
                        active.second.append(' ').append(rawLine.trim())
                    }
                } else {
                    description.append(rawLine).append('\n')
                }
            }
        }

        val unsupportedTags = mutableListOf<String>()
        val params = mutableListOf<Pair<String, String>>()
        var returns: String? = null

        blocks.forEach { (tag, body) ->
            val text = body.toString().trim()
            when (tag) {
                "param" -> {
                    val spaceIndex = text.indexOf(' ')
                    val name = if (spaceIndex == -1) text else text.substring(0, spaceIndex)
                    val desc = if (spaceIndex == -1) "" else text.substring(spaceIndex + 1).trim()
                    if (name.isNotBlank()) params += name to desc
                }

                "return", "returns" -> returns = text

                else -> unsupportedTags += tag
            }
        }

        val markdown = render(description.toString().trim(), params, returns, paramTypes)
        return KDocParseResult(markdown, unsupportedTags)
    }

    /** Strips `/** */` delimiters and per-line ` * ` continuation markers from a raw doc comment. */
    internal fun cleanDocString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("/**")) text = text.removePrefix("/**")
        if (text.endsWith("*/")) text = text.removeSuffix("*/")
        return text.lineSequence()
            .map { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("* ") -> trimmed.removePrefix("* ")
                    trimmed == "*" -> ""
                    trimmed.startsWith("*") -> trimmed.removePrefix("*")
                    else -> trimmed
                }
            }
            .joinToString("\n")
            .trim()
    }

    private fun render(
        description: String,
        params: List<Pair<String, String>>,
        returns: String?,
        paramTypes: Map<String, ParamTypeInfo>,
    ): String {
        val sb = StringBuilder()

        if (description.isNotBlank()) {
            sb.append(escapeLinks(description))
        }

        if (params.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append("\n\n")
            sb.append("**Parameters**\n")
            params.forEachIndexed { index, (name, desc) ->
                sb.append("- `").append(name).append('`')
                if (desc.isNotBlank()) {
                    sb.append(": ").append(escapeLinks(desc))
                }
                when (val info = paramTypes[name]) {
                    is ParamTypeInfo.EnumValues -> appendEntries(sb, info.entries)
                    is ParamTypeInfo.SealedSubtypes -> appendEntries(sb, info.subtypes)
                    else -> Unit
                }
                if (index < params.lastIndex) sb.append('\n')
            }
        }

        if (!returns.isNullOrBlank()) {
            if (sb.isNotEmpty()) sb.append("\n\n")
            sb.append("**Returns**\n").append(escapeLinks(returns))
        }

        return sb.toString().trim()
    }

    private fun appendEntries(sb: StringBuilder, entries: List<DocumentedEntry>) {
        entries.forEach { entry ->
            sb.append("\n  - **").append(entry.name).append("**")
            if (!entry.doc.isNullOrBlank()) {
                sb.append(" — ").append(escapeLinks(entry.doc))
            }
        }
    }

    private fun escapeLinks(text: String): String =
        kdocLinkRegex.replace(text) { match -> "`${match.groupValues[1]}`" }
}
