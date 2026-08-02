package io.cstories.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KotlinSyntaxHighlighterTest {

    @Test
    fun `tokenizes keywords`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("true false null")
        assertEquals(
            listOf("true", "false", "null"),
            tokens.filterIsInstance<KotlinToken.Keyword>().map { it.text },
        )
    }

    @Test
    fun `tokenizes a simple string literal without interpolation`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("\"Click me\"")
        val literal = tokens.single()
        assertIs<KotlinToken.StringLiteral>(literal)
        assertEquals(listOf(StringPart.Literal("Click me")), literal.parts)
    }

    @Test
    fun `tokenizes a string literal with simple interpolation`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("\"Hello \$label\"")
        val literal = tokens.single() as KotlinToken.StringLiteral
        assertEquals(
            listOf(StringPart.Literal("Hello "), StringPart.Interpolation("\$label")),
            literal.parts,
        )
    }

    @Test
    fun `tokenizes a string literal with braced interpolation`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("\"Hello \${label.uppercase()}\"")
        val literal = tokens.single() as KotlinToken.StringLiteral
        assertEquals(
            listOf(StringPart.Literal("Hello "), StringPart.Interpolation("\${label.uppercase()}")),
            literal.parts,
        )
    }

    @Test
    fun `tokenizes number literals with suffixes`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("1.5f 10L 0xFF")
        assertEquals(
            listOf("1.5f", "10L", "0xFF"),
            tokens.filterIsInstance<KotlinToken.NumberLiteral>().map { it.text },
        )
    }

    @Test
    fun `tokenizes annotations`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("@Composable")
        assertEquals(listOf("@Composable"), tokens.filterIsInstance<KotlinToken.Annotation>().map { it.text })
    }

    @Test
    fun `tokenizes line and block comments`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("// a comment\n/* block */")
        assertEquals(
            listOf("// a comment", "/* block */"),
            tokens.filterIsInstance<KotlinToken.Comment>().map { it.text },
        )
    }

    @Test
    fun `does not crash on generics`() {
        val tokens = KotlinSyntaxHighlighter.tokenize("listOf<String>()")
        assertTrue(tokens.isNotEmpty())
        assertTrue(tokens.filterIsInstance<KotlinToken.TypeOrFunctionCall>().any { it.text == "listOf" })
    }

    @Test
    fun `categorizes a real usage snippet end to end`() {
        val tokens = KotlinSyntaxHighlighter.tokenize(
            """PrimaryButton(text = "Click me", enabled = true, onClick = {})""",
        )

        assertEquals(
            "PrimaryButton",
            (tokens.first { it is KotlinToken.TypeOrFunctionCall } as KotlinToken.TypeOrFunctionCall).text,
        )
        assertTrue(tokens.filterIsInstance<KotlinToken.Plain>().any { it.text == "text" })
        assertTrue(tokens.filterIsInstance<KotlinToken.Plain>().any { it.text == "enabled" })
        assertTrue(tokens.filterIsInstance<KotlinToken.Plain>().any { it.text == "onClick" })
        assertTrue(tokens.filterIsInstance<KotlinToken.Keyword>().any { it.text == "true" })
        assertTrue(
            tokens.filterIsInstance<KotlinToken.StringLiteral>()
                .any { it.parts == listOf(StringPart.Literal("Click me")) },
        )
    }

    @Test
    fun `does not break on multi-line lambdas`() {
        val tokens = KotlinSyntaxHighlighter.tokenize(
            """
            PrimaryButton(
                text = label,
                onClick = {
                    doSomething()
                    doSomethingElse()
                },
            )
            """.trimIndent(),
        )
        assertTrue(tokens.isNotEmpty())
        assertTrue(tokens.filterIsInstance<KotlinToken.TypeOrFunctionCall>().any { it.text == "PrimaryButton" })
        assertTrue(tokens.filterIsInstance<KotlinToken.TypeOrFunctionCall>().any { it.text == "doSomething" })
    }
}
