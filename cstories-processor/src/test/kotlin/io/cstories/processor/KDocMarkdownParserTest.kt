package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KDocMarkdownParserTest {
    @Test
    fun `returns null for blank or null doc string`() {
        assertNull(KDocMarkdownParser.parse(null))
        assertNull(KDocMarkdownParser.parse("   "))
    }

    @Test
    fun `renders a simple description with no tags`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * A simple button.
             */
            """.trimIndent(),
        )

        assertEquals("A simple button.", result?.markdown)
        assertEquals(emptyList(), result?.unsupportedTags)
    }

    @Test
    fun `renders param and return tags`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * Does something useful.
             *
             * @param label The label to display.
             * @param enabled Whether it is enabled.
             * @return A result value.
             */
            """.trimIndent(),
        )

        assertEquals(
            """
            Does something useful.

            **Parameters**
            - `label`: The label to display.
            - `enabled`: Whether it is enabled.

            **Returns**
            A result value.
            """.trimIndent(),
            result?.markdown,
        )
    }

    @Test
    fun `reports unsupported tags without including them in the markdown`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * Description.
             *
             * @param x A param.
             * @see SomethingElse
             * @throws IllegalStateException sometimes
             */
            """.trimIndent(),
        )

        assertEquals(listOf("see", "throws"), result?.unsupportedTags)
        assertTrue(result?.markdown?.contains("SomethingElse") == false)
    }

    @Test
    fun `escapes kdoc reference links as inline code`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * See [OtherComponent] for details.
             */
            """.trimIndent(),
        )

        assertEquals("See `OtherComponent` for details.", result?.markdown)
    }

    @Test
    fun `enriches a param with enum values`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param size The size.
             */
            """.trimIndent(),
            mapOf(
                "size" to ParamTypeInfo.EnumValues(
                    listOf(
                        DocumentedEntry("Small", null),
                        DocumentedEntry("Large", "The large variant."),
                    ),
                ),
            ),
        )

        assertEquals(
            """
            **Parameters**
            - `size`: The size.
              - **Small**
              - **Large** — The large variant.
            """.trimIndent(),
            result?.markdown,
        )
    }

    @Test
    fun `enriches a param with sealed subtypes`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param adornment The adornment.
             */
            """.trimIndent(),
            mapOf(
                "adornment" to ParamTypeInfo.SealedSubtypes(
                    listOf(DocumentedEntry("Icon", "An icon adornment.")),
                ),
            ),
        )

        assertTrue(result?.markdown?.contains("- **Icon** — An icon adornment.") == true)
    }

    @Test
    fun `plain param type is not enriched`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param value The value.
             */
            """.trimIndent(),
            mapOf("value" to ParamTypeInfo.Plain),
        )

        assertEquals("**Parameters**\n- `value`: The value.", result?.markdown)
    }
}
