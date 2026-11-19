package io.cstories.runtime

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTextTest {

    @Test
    fun `parses a plain paragraph`() {
        val blocks = parseMarkdownBlocks("A simple description.")

        assertEquals(listOf(MarkdownBlock.Paragraph("A simple description.")), blocks)
    }

    @Test
    fun `parses an ATX heading distinct from a bold heading`() {
        val blocks = parseMarkdownBlocks("## Example")

        assertEquals(listOf(MarkdownBlock.Heading("Example", level = 2)), blocks)
    }

    @Test
    fun `keeps the ATX heading level and defaults bold headings below h3`() {
        val blocks = parseMarkdownBlocks("# Title\n\n## Section\n\n**Parameters**")

        assertEquals(
            listOf(
                MarkdownBlock.Heading("Title", level = 1),
                MarkdownBlock.Heading("Section", level = 2),
                MarkdownBlock.Heading("Parameters", level = BOLD_HEADING_LEVEL),
            ),
            blocks,
        )
    }

    @Test
    fun `parses an anchored heading`() {
        val blocks = parseMarkdownBlocks("### {#TabItem} TabItem")

        assertEquals(listOf(MarkdownBlock.Heading("TabItem", anchor = "TabItem", level = 3)), blocks)
    }

    @Test
    fun `parses a fenced kotlin code block preserving line breaks`() {
        val markdown = """
            ## Example

            ```kotlin
            PrimaryButton(
                text = "Ok",
            )
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(
            listOf(
                MarkdownBlock.Heading("Example", level = 2),
                MarkdownBlock.CodeBlock(
                    language = "kotlin",
                    code = "PrimaryButton(\n    text = \"Ok\",\n)",
                ),
            ),
            blocks,
        )
    }

    @Test
    fun `an unterminated fence is still emitted as a code block`() {
        val markdown = """
            ```kotlin
            val x = 1
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(listOf(MarkdownBlock.CodeBlock("kotlin", "val x = 1")), blocks)
    }

    @Test
    fun `code fence content is not treated as a table or a list`() {
        val markdown = """
            ```kotlin
            | a | b |
            - not a list
            ```
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(
            listOf(MarkdownBlock.CodeBlock("kotlin", "| a | b |\n- not a list")),
            blocks,
        )
    }

    @Test
    fun `parameters table is still parsed as a table`() {
        val markdown = """
            **Parameters**

            | Name | Type |
            | --- | --- |
            | `label` | `String` |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(
            listOf(
                MarkdownBlock.Heading("Parameters"),
                MarkdownBlock.Table(listOf("Name", "Type"), listOf(listOf("`label`", "`String`"))),
            ),
            blocks,
        )
    }

    @Test
    fun `groups the see also heading with its list items`() {
        val blocks = listOf(
            MarkdownBlock.Paragraph("Intro."),
            MarkdownBlock.Heading(SEE_ALSO_MARKER),
            MarkdownBlock.ListItem(0, "[Other](#Other)"),
            MarkdownBlock.ListItem(0, "`Unresolved`"),
            MarkdownBlock.Paragraph("Outro."),
        )

        assertEquals(
            listOf(
                MarkdownNode.Plain(MarkdownBlock.Paragraph("Intro.")),
                MarkdownNode.SeeAlso(
                    listOf(MarkdownBlock.ListItem(0, "[Other](#Other)"), MarkdownBlock.ListItem(0, "`Unresolved`")),
                ),
                MarkdownNode.Plain(MarkdownBlock.Paragraph("Outro.")),
            ),
            groupMarkdownBlocks(blocks),
        )
    }

    @Test
    fun `splits related declarations into one card per anchored heading`() {
        val markdown = """
            Main.

            ## $RELATED_MARKER

            ### {#TabItem} TabItem

            Tab doc.

            **Parameters**

            ### {#TabRow} TabRow

            Row doc.
        """.trimIndent()

        assertEquals(
            listOf(
                MarkdownNode.Plain(MarkdownBlock.Paragraph("Main.")),
                MarkdownNode.RelatedTitle,
                MarkdownNode.RelatedCard(
                    title = "TabItem",
                    anchor = "TabItem",
                    children = listOf(
                        MarkdownNode.Plain(MarkdownBlock.Paragraph("Tab doc.")),
                        MarkdownNode.Plain(MarkdownBlock.Heading("Parameters")),
                    ),
                ),
                MarkdownNode.RelatedCard(
                    title = "TabRow",
                    anchor = "TabRow",
                    children = listOf(MarkdownNode.Plain(MarkdownBlock.Paragraph("Row doc."))),
                ),
            ),
            groupMarkdownBlocks(parseMarkdownBlocks(markdown)),
        )
    }
}
