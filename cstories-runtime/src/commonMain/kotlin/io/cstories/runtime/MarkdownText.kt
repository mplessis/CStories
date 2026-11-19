package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.docs_example_copy_button_copied_description
import io.cstories.runtime.resources.docs_example_copy_button_description
import io.cstories.runtime.resources.docs_related_title
import io.cstories.runtime.resources.docs_see_also_title
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.jetbrains.compose.resources.stringResource

/**
 * Minimal, dependency-free Markdown renderer supporting the subset produced
 * by the KDoc-to-Markdown processor: paragraphs, `#` headings, `**bold**` section titles,
 * `- ` / `* ` / `+ ` bullet lists and `1.` / `1)` numbered lists (2-space nested
 * indentation, CommonMark-style renumbering), pipe tables
 * (`| Header | ... |` with a `| --- | ... |` separator row, cells may embed
 * `<br>` for multi-line content), inline `` `code` ``, `**bold**` / `__bold__`,
 * `*italic*` / `_italic_` spans, and
 * `[label](#anchor)` links scrolling to a `### {#anchor} Title` heading.
 * Deliberately hand-rolled (no third-party lib) to stay Kotlin/Wasm-friendly.
 */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val anchors = remember(blocks) {
        blocks.mapNotNull { (it as? MarkdownBlock.Heading)?.anchor }.associateWith { BringIntoViewRequester() }
    }
    val scope = rememberCoroutineScope()
    val onLink: (String) -> Unit = { anchor -> anchors[anchor]?.let { scope.launch { it.bringIntoView(anchorRevealRect) } } }
    val nodes = remember(blocks) { groupMarkdownBlocks(blocks) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        nodes.forEach { node ->
            when (node) {
                is MarkdownNode.RelatedTitle -> Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HorizontalDivider(color = CStoriesColors.border)
                    Text(
                        text = stringResource(Res.string.docs_related_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CStoriesColors.text,
                    )
                }

                is MarkdownNode.RelatedCard -> MarkdownRelatedCard(
                    card = node,
                    modifier = node.anchor?.let { anchors[it] }
                        ?.let { Modifier.bringIntoViewRequester(it) }
                        ?: Modifier,
                    onLink = onLink,
                )

                else -> MarkdownNodeView(node, onLink)
            }
        }
    }
}

@Composable
private fun MarkdownNodeView(node: MarkdownNode, onLink: (String) -> Unit) {
    when (node) {
        is MarkdownNode.Plain -> MarkdownBlockView(node.block, onLink)
        is MarkdownNode.SeeAlso -> MarkdownSeeAlso(node, onLink)
        is MarkdownNode.RelatedTitle, is MarkdownNode.RelatedCard -> Unit
    }
}

@Composable
private fun MarkdownBlockView(block: MarkdownBlock, onLink: (String) -> Unit) {
    when (block) {
        is MarkdownBlock.Heading -> MarkdownHeading(block, onLink)

        is MarkdownBlock.Paragraph -> Text(
            text = renderInline(block.text, onLink),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = CStoriesColors.textMuted,
        )

        is MarkdownBlock.ListItem -> MarkdownListItem(block, onLink)

        is MarkdownBlock.Table -> MarkdownTable(block, onLink)

        is MarkdownBlock.CodeBlock -> MarkdownExampleCodeBlock(block.code)
    }
}

@Composable
private fun MarkdownHeading(heading: MarkdownBlock.Heading, onLink: (String) -> Unit) {
    val (fontSize, topPadding) = when (heading.level) {
        1 -> 22.sp to 12.dp
        2 -> 18.sp to 10.dp
        3 -> 15.sp to 6.dp
        else -> 13.sp to 0.dp
    }
    Column(
        modifier = Modifier.padding(top = topPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = renderInline(heading.text, onLink),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize * 1.3f,
            color = CStoriesColors.text,
        )
        if (heading.level <= 2) HorizontalDivider(color = CStoriesColors.border)
    }
}

@Composable
private fun MarkdownListItem(item: MarkdownBlock.ListItem, onLink: (String) -> Unit) {
    Row(modifier = Modifier.padding(start = (item.indent * 14).dp)) {
        Text(
            text = item.ordinal?.let { "$it.  " } ?: "•  ",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = CStoriesColors.textFaint,
        )
        Text(
            text = renderInline(item.text, onLink),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = CStoriesColors.textMuted,
        )
    }
}

@Composable
private fun MarkdownSeeAlso(seeAlso: MarkdownNode.SeeAlso, onLink: (String) -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(CStoriesColors.surfaceMuted),
    ) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(CStoriesColors.primary))
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.docs_see_also_title),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CStoriesColors.text,
            )
            seeAlso.items.forEach { MarkdownListItem(it, onLink) }
        }
    }
}

@Composable
private fun MarkdownRelatedCard(card: MarkdownNode.RelatedCard, modifier: Modifier, onLink: (String) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(CStoriesColors.surfaceMuted)
            .border(1.dp, CStoriesColors.border, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = renderInline(card.title, onLink),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = CStoriesColors.text,
        )
        card.children.forEach { MarkdownNodeView(it, onLink) }
    }
}

@Composable
private fun MarkdownExampleCodeBlock(code: String) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember(code) { mutableStateOf(false) }
    val copyDescription = stringResource(Res.string.docs_example_copy_button_description)
    val copiedDescription = stringResource(Res.string.docs_example_copy_button_copied_description)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        CodeBlock(
            code = code,
            modifier = Modifier.fillMaxWidth(),
        )
        IconButton(
            onClick = {
                copied = true
                scope.launch {
                    clipboard.setPlainText(code)
                    delay(1500)
                    copied = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .semantics {
                    contentDescription = if (copied) copiedDescription else copyDescription
                },
        ) {
            Icon(
                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = null,
                tint = CStoriesColors.primary,
            )
        }
    }
}

@Composable
private fun MarkdownTable(table: MarkdownBlock.Table, onLink: (String) -> Unit) {
    val growIndex = table.headers.indexOf("Description").takeIf { it >= 0 }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, CStoriesColors.border),
    ) {
        MarkdownTableRow(table.headers, bold = true, growIndex = growIndex, onLink = onLink)
        table.rows.forEach { row ->
            HorizontalDivider(color = CStoriesColors.border)
            MarkdownTableRow(row, bold = false, growIndex = growIndex, onLink = onLink)
        }
    }
}

@Composable
private fun MarkdownTableRow(cells: List<String>, bold: Boolean, growIndex: Int?, onLink: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) VerticalDivider(color = CStoriesColors.border, modifier = Modifier.fillMaxHeight())
            Column(
                modifier = Modifier
                    .then(
                        if (index == growIndex) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.width(if (index == 0) 220.dp else 160.dp)
                        },
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                cell.split("<br>").forEach { line ->
                    Text(
                        text = renderInline(line, onLink),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                        color = if (bold) CStoriesColors.text else CStoriesColors.textMuted,
                    )
                }
            }
        }
    }
}

internal sealed interface MarkdownBlock {
    /** [level] follows ATX `#` count (1–6); `**bold**` section titles use [BOLD_HEADING_LEVEL]. */
    data class Heading(val text: String, val anchor: String? = null, val level: Int = BOLD_HEADING_LEVEL) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    /** A list item; [ordinal] is the displayed number for ordered items, `null` for bullets. */
    data class ListItem(val indent: Int, val text: String, val ordinal: Int? = null) : MarkdownBlock
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock

    /** A fenced ` ```language ` … ` ``` ` code block, rendered with [CodeBlock]'s Kotlin syntax highlighting. */
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
}

/**
 * Display-level grouping of [MarkdownBlock]s, so the `See also` list and the
 * related declarations sections can be rendered distinctly from the rest of
 * the documentation. Built by [groupMarkdownBlocks].
 */
internal sealed interface MarkdownNode {
    data class Plain(val block: MarkdownBlock) : MarkdownNode
    data class SeeAlso(val items: List<MarkdownBlock.ListItem>) : MarkdownNode
    data object RelatedTitle : MarkdownNode
    data class RelatedCard(val title: String, val anchor: String?, val children: List<MarkdownNode>) : MarkdownNode
}

internal fun groupMarkdownBlocks(blocks: List<MarkdownBlock>): List<MarkdownNode> {
    val relatedIndex = blocks.indexOfFirst { it is MarkdownBlock.Heading && it.text == RELATED_MARKER }
    if (relatedIndex == -1) return groupSeeAlso(blocks)

    val nodes = groupSeeAlso(blocks.subList(0, relatedIndex)).toMutableList()
    nodes += MarkdownNode.RelatedTitle
    var cardHeading: MarkdownBlock.Heading? = null
    val cardChildren = mutableListOf<MarkdownBlock>()
    fun flushCard() {
        val heading = cardHeading
        if (heading != null) {
            nodes += MarkdownNode.RelatedCard(heading.text, heading.anchor, groupSeeAlso(cardChildren.toList()))
        } else {
            nodes += groupSeeAlso(cardChildren.toList())
        }
        cardChildren.clear()
    }
    blocks.subList(relatedIndex + 1, blocks.size).forEach { block ->
        if (block is MarkdownBlock.Heading && block.anchor != null) {
            flushCard()
            cardHeading = block
        } else {
            cardChildren += block
        }
    }
    flushCard()
    return nodes
}

private fun groupSeeAlso(blocks: List<MarkdownBlock>): List<MarkdownNode> {
    val nodes = mutableListOf<MarkdownNode>()
    var i = 0
    while (i < blocks.size) {
        val block = blocks[i]
        if (block is MarkdownBlock.Heading && block.text == SEE_ALSO_MARKER) {
            val items = blocks.drop(i + 1).takeWhile { it is MarkdownBlock.ListItem }.filterIsInstance<MarkdownBlock.ListItem>()
            nodes += MarkdownNode.SeeAlso(items)
            i += 1 + items.size
        } else {
            nodes += MarkdownNode.Plain(block)
            i++
        }
    }
    return nodes
}

private sealed interface MarkdownLine {
    data class Heading(val text: String, val anchor: String? = null, val level: Int = BOLD_HEADING_LEVEL) : MarkdownLine
    data class ListItem(val indent: Int, val text: String, val number: Int? = null) : MarkdownLine
    data class TableRow(val cells: List<String>) : MarkdownLine
    data class Text(val text: String) : MarkdownLine
    data object Blank : MarkdownLine
}

private val tableSeparatorRegex = Regex("^\\|(\\s*:?-+:?\\s*\\|)+$")
private val codeFenceRegex = Regex("^```(\\w*)\\s*$")

/**
 * Inline marker emitted by `KDocMarkdownParser` (in `cstories-processor`)
 * right after a required parameter's name in the `Parameters` table.
 * Rendered here as a red asterisk. Must stay in sync with
 * `KDocMarkdownParser.REQUIRED_MARKER`.
 */
private const val REQUIRED_MARKER = "\u00A4req\u00A4"

/**
 * Heading emitted by `KDocMarkdownParser` above the sections documenting
 * the declarations linked from a component's KDoc. Rendered here as a
 * localized title. Must stay in sync with `KDocMarkdownParser.RELATED_MARKER`.
 */
internal const val RELATED_MARKER = "\u00A4related\u00A4"

/**
 * Heading emitted by `KDocMarkdownParser` above a `@see` list. Rendered here
 * as a localized callout. Must stay in sync with
 * `KDocMarkdownParser.SEE_ALSO_MARKER`.
 */
internal const val SEE_ALSO_MARKER = "\u00A4seealso\u00A4"

internal const val BOLD_HEADING_LEVEL = 4

// Taller than any viewport so the anchored heading is aligned to the top of the scrolled area.
private val anchorRevealRect = Rect(0f, 0f, 1f, 100_000f)

private val orderedItemRegex = Regex("^(\\d{1,9})[.)]\\s+(\\S.*)$")
private val headingAnchorRegex = Regex("^\\{#([^}\\s]+)}")
private val inlineLinkRegex = Regex("^\\[([^]]+)]\\(#([^)\\s]+)\\)")

private fun classifyLine(line: String): MarkdownLine {
    if (line.isBlank()) return MarkdownLine.Blank
    val leadingSpaces = line.takeWhile { it == ' ' }.length
    val stripped = line.trim()
    if (stripped.length > 2 && stripped[0] in "-*+" && stripped[1] == ' ') {
        return MarkdownLine.ListItem(indent = leadingSpaces / 2, text = stripped.substring(2).trim())
    }
    orderedItemRegex.matchEntire(stripped)?.let { match ->
        return MarkdownLine.ListItem(
            indent = leadingSpaces / 2,
            text = match.groupValues[2].trim(),
            number = match.groupValues[1].toInt(),
        )
    }
    if (stripped.startsWith("**") && stripped.endsWith("**") && stripped.length > 4 && stripped.count { it == '*' } == 4) {
        return MarkdownLine.Heading(stripped.trim('*'))
    }
    if (stripped.startsWith("#")) {
        val level = stripped.takeWhile { it == '#' }.length
        val text = stripped.drop(level).trim()
        if (level in 1..6 && text.isNotEmpty()) {
            val anchorMatch = headingAnchorRegex.find(text)
            if (anchorMatch != null) {
                return MarkdownLine.Heading(text.substring(anchorMatch.range.last + 1).trim(), anchorMatch.groupValues[1], level)
            }
            return MarkdownLine.Heading(text, level = level)
        }
    }
    if (stripped.startsWith("|") && stripped.endsWith("|")) {
        return MarkdownLine.TableRow(splitTableCells(stripped))
    }
    return MarkdownLine.Text(stripped)
}

/** Splits a `| a | b |` row into `["a", "b"]`, honoring `\|` as an escaped pipe. */
private fun splitTableCells(row: String): List<String> {
    val inner = row.removePrefix("|").removeSuffix("|")
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var i = 0
    while (i < inner.length) {
        val c = inner[i]
        if (c == '\\' && i + 1 < inner.length && inner[i + 1] == '|') {
            current.append('|')
            i += 2
        } else if (c == '|') {
            cells += current.toString().trim()
            current.clear()
            i++
        } else {
            current.append(c)
            i++
        }
    }
    cells += current.toString().trim()
    return cells
}

internal fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphBuffer = mutableListOf<String>()
    val pendingTableHeader = mutableListOf<List<String>>()
    var tableHeaders: List<String>? = null
    val tableRows = mutableListOf<List<String>>()

    fun flushParagraph() {
        if (paragraphBuffer.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraphBuffer.joinToString(" "))
            paragraphBuffer.clear()
        }
    }

    fun flushTable() {
        val headers = tableHeaders
        if (headers != null) {
            blocks += MarkdownBlock.Table(headers, tableRows.toList())
        }
        tableHeaders = null
        tableRows.clear()
        pendingTableHeader.clear()
    }

    var codeFenceLanguage: String? = null
    var codeFenceLines: MutableList<String>? = null

    markdown.lines().forEach { rawLine ->
        val activeFenceLines = codeFenceLines
        if (activeFenceLines != null) {
            val closingMatch = codeFenceRegex.find(rawLine.trim())
            if (closingMatch != null) {
                blocks += MarkdownBlock.CodeBlock(codeFenceLanguage, activeFenceLines.joinToString("\n"))
                codeFenceLines = null
                codeFenceLanguage = null
            } else {
                activeFenceLines += rawLine
            }
            return@forEach
        }

        val openingMatch = codeFenceRegex.find(rawLine.trim())
        if (openingMatch != null) {
            flushParagraph()
            flushTable()
            codeFenceLanguage = openingMatch.groupValues[1].takeIf { it.isNotBlank() }
            codeFenceLines = mutableListOf()
            return@forEach
        }

        when (val line = classifyLine(rawLine)) {
            is MarkdownLine.Blank -> {
                flushParagraph()
                flushTable()
            }

            is MarkdownLine.Heading -> {
                flushParagraph()
                flushTable()
                blocks += MarkdownBlock.Heading(line.text, line.anchor, line.level)
            }

            is MarkdownLine.ListItem -> {
                flushParagraph()
                flushTable()
                blocks += MarkdownBlock.ListItem(line.indent, line.text, line.number)
            }

            is MarkdownLine.TableRow -> {
                flushParagraph()
                when {
                    tableHeaders == null && pendingTableHeader.isEmpty() -> pendingTableHeader += line.cells
                    tableHeaders == null && pendingTableHeader.isNotEmpty() -> {
                        val separatorCandidate = rawLine.trim()
                        if (tableSeparatorRegex.matches(separatorCandidate)) {
                            tableHeaders = pendingTableHeader.first()
                        } else {
                            // Not a real table (no separator row): flush the pending header line as text.
                            paragraphBuffer += pendingTableHeader.first().joinToString(" | ")
                            pendingTableHeader.clear()
                            paragraphBuffer += line.cells.joinToString(" | ")
                        }
                    }

                    else -> tableRows += line.cells
                }
            }

            is MarkdownLine.Text -> {
                flushTable()
                paragraphBuffer += line.text
            }
        }
    }
    flushParagraph()
    flushTable()
    codeFenceLines?.let { blocks += MarkdownBlock.CodeBlock(codeFenceLanguage, it.joinToString("\n")) }

    return renumberOrderedLists(blocks)
}

/**
 * CommonMark-style numbering: an ordered list starts at its first written
 * number and increments from there, whatever the following items say. A list
 * ends at any non-list block, a bullet at the same indent, or a shallower item.
 */
private fun renumberOrderedLists(blocks: List<MarkdownBlock>): List<MarkdownBlock> {
    val counters = mutableMapOf<Int, Int>()
    return blocks.map { block ->
        if (block !is MarkdownBlock.ListItem) {
            counters.clear()
            return@map block
        }
        counters.keys.removeAll { it > block.indent }
        val written = block.ordinal
        if (written == null) {
            counters.remove(block.indent)
            block
        } else {
            val ordinal = counters[block.indent]?.plus(1) ?: written
            counters[block.indent] = ordinal
            block.copy(ordinal = ordinal)
        }
    }
}


private val linkStyles = TextLinkStyles(
    style = SpanStyle(color = CStoriesColors.primary, textDecoration = TextDecoration.Underline),
)

internal fun renderInline(text: String, onLink: ((String) -> Unit)? = null): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        val link = if (text[i] == '[') inlineLinkRegex.find(text.substring(i)) else null
        when {
            link != null -> {
                val anchor = link.groupValues[2]
                val label = renderInline(link.groupValues[1])
                if (onLink != null) {
                    pushLink(LinkAnnotation.Clickable(tag = anchor, styles = linkStyles) { onLink(anchor) })
                    append(label)
                    pop()
                } else {
                    append(label)
                }
                i += link.value.length
            }

            text.startsWith(REQUIRED_MARKER, i) -> {
                pushStyle(SpanStyle(color = Color.Red))
                append("*")
                pop()
                i += REQUIRED_MARKER.length
            }

            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end == -1) {
                    append(text.substring(i))
                    i = text.length
                } else {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                }
            }

            else -> {
                val emphasis = findEmphasis(text, i)
                if (emphasis != null) {
                    pushStyle(emphasis.style)
                    append(renderInline(text.substring(emphasis.contentStart, emphasis.contentEnd), onLink))
                    pop()
                    i = emphasis.end
                } else {
                    // Consume at least one char so an unmatched delimiter is emitted literally.
                    val nextSpecial = (i + 1 until text.length).firstOrNull { text[it] in inlineSpecialChars } ?: text.length
                    append(text.substring(i, nextSpecial))
                    i = nextSpecial
                }
            }
        }
    }
}

private const val inlineSpecialChars = "*_`[\u00A4"

private class Emphasis(val style: SpanStyle, val contentStart: Int, val contentEnd: Int, val end: Int)

private val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
private val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)

/**
 * Matches `**bold**`, `__bold__`, `*italic*` or `_italic_` starting at [start].
 * Underscore delimiters only apply at word boundaries so `snake_case` stays literal,
 * and single delimiters must hug their content (`a * b` is not italic).
 */
private fun findEmphasis(text: String, start: Int): Emphasis? {
    val char = text[start]
    if (char != '*' && char != '_') return null
    val double = text.startsWith("$char$char", start)
    val delimiter = if (double) "$char$char" else "$char"
    val underscore = char == '_'
    val contentStart = start + delimiter.length
    if (contentStart >= text.length || text[contentStart].isWhitespace()) return null
    if (underscore && start > 0 && text[start - 1].isLetterOrDigit()) return null

    var j = text.indexOf(delimiter, contentStart + 1)
    while (j != -1) {
        val after = j + delimiter.length
        val hugsContent = !text[j - 1].isWhitespace()
        val closesWord = !underscore || after >= text.length || !text[after].isLetterOrDigit()
        val notLonger = after >= text.length || text[after] != char
        val notShorter = double || text[j - 1] != char
        if (hugsContent && closesWord && notLonger && notShorter) {
            return Emphasis(if (double) boldStyle else italicStyle, contentStart, j, after)
        }
        j = text.indexOf(delimiter, j + 1)
    }
    return null
}
