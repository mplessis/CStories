package io.cstories.runtime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal, dependency-free Markdown renderer supporting the subset produced
 * by the KDoc-to-Markdown processor: paragraphs, `**bold**` section titles,
 * `- ` bullet lists (with 2-space nested indentation), and inline `` `code` ``
 * / `**bold**` spans. Deliberately hand-rolled (no third-party lib) to stay
 * Kotlin/Wasm-friendly.
 */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = renderInline(block.text),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CStoriesColors.text,
                )

                is MarkdownBlock.Paragraph -> Text(
                    text = renderInline(block.text),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = CStoriesColors.textMuted,
                )

                is MarkdownBlock.ListItem -> Row(
                    modifier = Modifier.padding(start = (block.indent * 14).dp),
                ) {
                    Text("•  ", fontSize = 13.sp, color = CStoriesColors.textFaint)
                    Text(
                        text = renderInline(block.text),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = CStoriesColors.textMuted,
                    )
                }
            }
        }
    }
}

private sealed interface MarkdownBlock {
    data class Heading(val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(val indent: Int, val text: String) : MarkdownBlock
}

private sealed interface MarkdownLine {
    data class Heading(val text: String) : MarkdownLine
    data class ListItem(val indent: Int, val text: String) : MarkdownLine
    data class Text(val text: String) : MarkdownLine
    data object Blank : MarkdownLine
}

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
    return MarkdownLine.Text(stripped)
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphBuffer = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraphBuffer.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraphBuffer.joinToString(" "))
            paragraphBuffer.clear()
        }
    }

    markdown.lines().forEach { rawLine ->
        when (val line = classifyLine(rawLine)) {
            is MarkdownLine.Blank -> flushParagraph()
            is MarkdownLine.Heading -> {
                flushParagraph()
                blocks += MarkdownBlock.Heading(line.text)
            }

            is MarkdownLine.ListItem -> {
                flushParagraph()
                blocks += MarkdownBlock.ListItem(line.indent, line.text)
            }

            is MarkdownLine.Text -> paragraphBuffer += line.text
        }
    }
    flushParagraph()

    return blocks
}

private fun renderInline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
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
                val candidates = listOf(text.indexOf("**", i), text.indexOf('`', i)).filter { it != -1 }
                val nextSpecial = candidates.minOrNull() ?: text.length
                append(text.substring(i, nextSpecial))
                i = nextSpecial
            }
        }
    }
}
