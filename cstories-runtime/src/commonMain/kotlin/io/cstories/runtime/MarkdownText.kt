package io.cstories.runtime

import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.docs_example_copy_button_copied_description
import io.cstories.runtime.resources.docs_example_copy_button_description
import io.cstories.runtime.resources.docs_related_title
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
 * by the KDoc-to-Markdown processor: paragraphs, `**bold**` section titles,
 * `- ` bullet lists (with 2-space nested indentation), pipe tables
 * (`| Header | ... |` with a `| --- | ... |` separator row, cells may embed
 * `<br>` for multi-line content), inline `` `code` `` / `**bold**` spans, and
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
    val relatedTitle = stringResource(Res.string.docs_related_title)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = if (block.text == RELATED_MARKER) AnnotatedString(relatedTitle) else renderInline(block.text, onLink),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (block.text == RELATED_MARKER) 15.sp else 13.sp,
                    color = CStoriesColors.text,
                    modifier = block.anchor?.let { anchors[it] }
                        ?.let { Modifier.padding(top = 8.dp).bringIntoViewRequester(it) }
                        ?: Modifier,
                )

                is MarkdownBlock.Paragraph -> Text(
                    text = renderInline(block.text, onLink),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = CStoriesColors.textMuted,
                )

                is MarkdownBlock.ListItem -> Row(
                    modifier = Modifier.padding(start = (block.indent * 14).dp),
                ) {
                    Text("•  ", fontSize = 13.sp, color = CStoriesColors.textFaint)
                    Text(
                        text = renderInline(block.text, onLink),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = CStoriesColors.textMuted,
                    )
                }

                is MarkdownBlock.Table -> MarkdownTable(block, onLink)

                is MarkdownBlock.CodeBlock -> MarkdownExampleCodeBlock(block.code)
            }
        }
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
    data class Heading(val text: String, val anchor: String? = null) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(val indent: Int, val text: String) : MarkdownBlock
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock

    /** A fenced ` ```language ` … ` ``` ` code block, rendered with [CodeBlock]'s Kotlin syntax highlighting. */
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
}

private sealed interface MarkdownLine {
    data class Heading(val text: String, val anchor: String? = null) : MarkdownLine
    data class ListItem(val indent: Int, val text: String) : MarkdownLine
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
private const val RELATED_MARKER = "\u00A4related\u00A4"

// Taller than any viewport so the anchored heading is aligned to the top of the scrolled area.
private val anchorRevealRect = Rect(0f, 0f, 1f, 100_000f)

private val headingAnchorRegex = Regex("^\\{#([^}\\s]+)}")
private val inlineLinkRegex = Regex("^\\[([^]]+)]\\(#([^)\\s]+)\\)")

private fun classifyLine(line: String): MarkdownLine {
    if (line.isBlank()) return MarkdownLine.Blank
    val leadingSpaces = line.takeWhile { it == ' ' }.length
    val stripped = line.trim()
    if (stripped.startsWith("- ")) {
        return MarkdownLine.ListItem(indent = leadingSpaces / 2, text = stripped.removePrefix("- ").trim())
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
                return MarkdownLine.Heading(text.substring(anchorMatch.range.last + 1).trim(), anchorMatch.groupValues[1])
            }
            return MarkdownLine.Heading(text)
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
                blocks += MarkdownBlock.Heading(line.text, line.anchor)
            }

            is MarkdownLine.ListItem -> {
                flushParagraph()
                flushTable()
                blocks += MarkdownBlock.ListItem(line.indent, line.text)
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

    return blocks
}


private val linkStyles = TextLinkStyles(
    style = SpanStyle(color = CStoriesColors.primary, textDecoration = TextDecoration.Underline),
)

private fun renderInline(text: String, onLink: ((String) -> Unit)? = null): AnnotatedString = buildAnnotatedString {
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

            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end == -1) {
                    append(text.substring(i))
                    i = text.length
                } else {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                }
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
                val candidates = listOf(
                    text.indexOf("**", i),
                    text.indexOf('`', i),
                    text.indexOf(REQUIRED_MARKER, i),
                    text.indexOf('[', i + 1),
                ).filter { it != -1 }
                val nextSpecial = candidates.minOrNull() ?: text.length
                append(text.substring(i, nextSpecial))
                i = nextSpecial
            }
        }
    }
}
