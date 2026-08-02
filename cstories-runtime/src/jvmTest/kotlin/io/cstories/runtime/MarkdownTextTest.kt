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

        assertEquals(listOf(MarkdownBlock.Heading("Example")), blocks)
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
                MarkdownBlock.Heading("Example"),
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
}
