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
 * Supports the description paragraph(s), `@param`, `@property`, `@see` and
 * `@return`/`@returns` tags. Any other tag (`@throws`, `@sample`...) is
 * reported back via [KDocParseResult.unsupportedTags] so the caller can emit
 * a KSP warning, but does not appear in the rendered output in this version.
 *
 * [paramTypes] optionally enriches each `@param` entry with the resolved
 * type's possible values (enum entries) or variants (sealed subtypes).
 *
 * [relatedDocs] are the declarations explicitly linked from the KDoc (see
 * [KDocLinkResolver]): links pointing to them become `[text](#anchor)`
 * Markdown links, and each one is rendered in an anchored section under a
 * [RELATED_MARKER] heading. Unresolved links stay as inline code.
 */
internal object KDocMarkdownParser {
    private val tagRegex = Regex("^@(\\w+)\\s*(.*)$")
    private val kdocLinkRegex = Regex("\\[([^]]+)]")

    /**
     * Inline marker emitted right after a required parameter's name in the
     * `Parameters` table. Interpreted by `MarkdownText`'s inline renderer
     * (in `cstories-runtime`) as a red asterisk; kept as a private token here
     * so it never collides with user-authored Markdown/KDoc content.
     */
    internal const val REQUIRED_MARKER = "\u00A4req\u00A4"

    /**
     * Heading text emitted above the related declarations sections.
     * Replaced by a localized title by `MarkdownText` (in `cstories-runtime`).
     */
    internal const val RELATED_MARKER = "\u00A4related\u00A4"

    fun parse(
        docString: String?,
        paramTypes: Map<String, ParamMetadata> = emptyMap(),
        relatedDocs: List<RelatedDoc> = emptyList(),
    ): KDocParseResult? {
        val links = relatedDocs.flatMap { doc -> doc.linkTexts.map { it to doc.anchor } }.toMap()
        val main = parseSingle(docString, paramTypes, links) ?: return null
        if (relatedDocs.isEmpty()) return main

        val sb = StringBuilder(main.markdown)
        sb.append("\n\n## ").append(RELATED_MARKER)
        relatedDocs.forEach { doc ->
            sb.append("\n\n### {#").append(doc.anchor).append("} ").append(doc.title)
            parseSingle(doc.docString, emptyMap(), links)?.markdown?.takeIf { it.isNotBlank() }?.let {
                sb.append("\n\n").append(it)
            }
        }
        return KDocParseResult(sb.toString(), main.unsupportedTags)
    }

    private fun parseSingle(
        docString: String?,
        paramTypes: Map<String, ParamMetadata>,
        links: Map<String, String>,
    ): KDocParseResult? {
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
        val properties = mutableListOf<Pair<String, String>>()
        val seeAlso = mutableListOf<String>()
        var returns: String? = null

        blocks.forEach { (tag, body) ->
            val text = body.toString().trim()
            when (tag) {
                "param" -> splitNameAndDescription(text)?.let { params += it }

                "property" -> splitNameAndDescription(text)?.let { properties += it }

                "see" -> if (text.isNotBlank()) seeAlso += text

                "return", "returns" -> returns = text

                else -> unsupportedTags += tag
            }
        }

        val markdown = Renderer(links).render(description.toString().trim(), params, properties, seeAlso, returns, paramTypes)
        return KDocParseResult(markdown, unsupportedTags)
    }

    private fun splitNameAndDescription(text: String): Pair<String, String>? {
        val spaceIndex = text.indexOf(' ')
        val name = if (spaceIndex == -1) text else text.substring(0, spaceIndex)
        val desc = if (spaceIndex == -1) "" else text.substring(spaceIndex + 1).trim()
        return if (name.isNotBlank()) name to desc else null
    }

    /**
     * Matches the leading KDoc continuation marker of a line: optional
     * indentation, the `*`, and at most one following space. Any further
     * whitespace after that single space is intentional content indentation
     * (e.g. inside a fenced code block) and must be preserved as-is.
     *
     * Only used as a fallback for raw doc comments that still carry their
     * literal `*` gutter (e.g. in unit tests). KSP's `KSDeclaration.docString`
     * already strips the `/** */` delimiters and the `*` markers themselves,
     * leaving each line with a single leading space (the former gutter gap)
     * plus any original extra indentation — see [stripLeadingMarker].
     */
    private val kdocLinePrefixRegex = Regex("^\\s*\\*\\s?")

    /** Strips `/** */` delimiters and per-line ` * ` continuation markers from a raw doc comment. */
    internal fun cleanDocString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("/**")) text = text.removePrefix("/**")
        if (text.endsWith("*/")) text = text.removeSuffix("*/")
        return text.lineSequence()
            .map(::stripLeadingMarker)
            .joinToString("\n")
            .trim()
    }

    /**
     * Removes a single KDoc line's leading marker, preserving any further
     * indentation. Handles both a raw doc comment that still has its literal
     * `*` gutter (e.g. `"* text"`, stripped via [kdocLinePrefixRegex]) and
     * KSP's already-normalized `docString` (only a single leading space to
     * drop, no `*` present).
     */
    private fun stripLeadingMarker(line: String): String {
        val match = kdocLinePrefixRegex.find(line)
        return if (match != null) line.substring(match.value.length).trimEnd() else line.removePrefix(" ").trimEnd()
    }

    private class Renderer(private val links: Map<String, String>) {
        fun render(
            description: String,
            params: List<Pair<String, String>>,
            properties: List<Pair<String, String>>,
            seeAlso: List<String>,
            returns: String?,
            paramTypes: Map<String, ParamMetadata>,
        ): String {
            val sb = StringBuilder()

            if (description.isNotBlank()) {
                sb.append(escapeLinks(description))
            }

            if (params.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append("\n\n")
                sb.append("**Parameters**\n\n")
                sb.append("| Name | Type | Default | Description | Possible values |\n")
                sb.append("| --- | --- | --- | --- | --- |\n")
                params.forEach { (name, desc) ->
                    val cellDescription = if (desc.isNotBlank()) escapeLinks(desc) else ""
                    val metadata = paramTypes[name]
                    val values = when (val info = metadata?.structural) {
                        is ParamTypeInfo.EnumValues -> entriesToCell(info.entries)
                        is ParamTypeInfo.SealedSubtypes -> entriesToCell(info.subtypes)
                        else -> ""
                    }
                    val requiredMarker = if (metadata?.required == true) REQUIRED_MARKER else ""
                    val typeName = metadata?.typeName ?: ""
                    val defaultCell = metadata?.defaultValue?.let { "`$it`" } ?: ""
                    sb.append("| `").append(name).append("`").append(requiredMarker).append(" | ")
                        .append("`").append(typeName).append("` | ")
                        .append(defaultCell).append(" | ")
                        .append(escapeCell(cellDescription)).append(" | ")
                        .append(escapeCell(values)).append(" |\n")
                }
                sb.setLength(sb.length - 1)
            }

            if (properties.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append("\n\n")
                sb.append("**Properties**\n\n")
                sb.append("| Name | Description |\n")
                sb.append("| --- | --- |\n")
                properties.forEach { (name, desc) ->
                    sb.append("| `").append(name).append("` | ").append(escapeCell(escapeLinks(desc))).append(" |\n")
                }
                sb.setLength(sb.length - 1)
            }

            if (seeAlso.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append("\n\n")
                sb.append("**See also**\n")
                seeAlso.forEach { entry ->
                    val target = entry.substringBefore(' ')
                    val rest = entry.substringAfter(' ', "").trim()
                    val bracketed = if (target.startsWith("[")) target else "[$target]"
                    sb.append("\n- ").append(escapeLinks(bracketed))
                    if (rest.isNotEmpty()) sb.append(" — ").append(escapeLinks(rest))
                }
            }

            if (!returns.isNullOrBlank()) {
                if (sb.isNotEmpty()) sb.append("\n\n")
                sb.append("**Returns**\n").append(escapeLinks(returns))
            }

            return sb.toString().trim()
        }

        private fun entriesToCell(entries: List<DocumentedEntry>): String =
            entries.joinToString("<br>") { entry ->
                val name = "**${entry.name}**"
                if (!entry.doc.isNullOrBlank()) "$name — ${escapeLinks(entry.doc)}" else name
            }

        private fun escapeLinks(text: String): String =
            kdocLinkRegex.replace(text) { match ->
                val target = match.groupValues[1]
                val anchor = links[target]
                if (anchor != null) "[`$target`](#$anchor)" else "`$target`"
            }
    }

    /** Escapes literal `|` characters so they don't break the Markdown table cell boundaries. */
    private fun escapeCell(text: String): String = text.replace("|", "\\|")
}
