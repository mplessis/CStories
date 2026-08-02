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

            | Name | Type | Default | Description | Possible values |
            | --- | --- | --- | --- | --- |
            | `label` | `` |  | The label to display. |  |
            | `enabled` | `` |  | Whether it is enabled. |  |

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
    fun `preserves code indentation inside a fenced code block`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * ## Example
             *
             * ```kotlin
             * PrimaryButton(
             *     text = "Ok",
             * )
             * ```
             */
            """.trimIndent(),
        )

        assertEquals(
            """
            ## Example

            ```kotlin
            PrimaryButton(
                text = "Ok",
            )
            ```
            """.trimIndent(),
            result?.markdown,
        )
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
                "size" to ParamMetadata(
                    typeName = "Size",
                    required = false,
                    structural = ParamTypeInfo.EnumValues(
                        listOf(
                            DocumentedEntry("Small", null),
                            DocumentedEntry("Large", "The large variant."),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            """
            **Parameters**

            | Name | Type | Default | Description | Possible values |
            | --- | --- | --- | --- | --- |
            | `size` | `Size` |  | The size. | **Small**<br>**Large** — The large variant. |
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
                "adornment" to ParamMetadata(
                    typeName = "Adornment",
                    required = false,
                    structural = ParamTypeInfo.SealedSubtypes(
                        listOf(DocumentedEntry("Icon", "An icon adornment.")),
                    ),
                ),
            ),
        )

        assertTrue(result?.markdown?.contains("**Icon** — An icon adornment.") == true)
    }

    @Test
    fun `plain param type is not enriched`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param value The value.
             */
            """.trimIndent(),
            mapOf("value" to ParamMetadata(typeName = "String", required = false, structural = ParamTypeInfo.Plain)),
        )

        assertEquals(
            "**Parameters**\n\n| Name | Type | Default | Description | Possible values |\n| --- | --- | --- | --- | --- |\n| `value` | `String` |  | The value. |  |",
            result?.markdown,
        )
    }

    @Test
    fun `renders the default value column when available`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param enabled Whether it is enabled.
             */
            """.trimIndent(),
            mapOf(
                "enabled" to ParamMetadata(
                    typeName = "Boolean",
                    required = false,
                    structural = ParamTypeInfo.Plain,
                    defaultValue = "true",
                ),
            ),
        )

        assertEquals(
            "**Parameters**\n\n| Name | Type | Default | Description | Possible values |\n| --- | --- | --- | --- | --- |\n| `enabled` | `Boolean` | `true` | Whether it is enabled. |  |",
            result?.markdown,
        )
    }

    @Test
    fun `omits the default value cell when there is none`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param value The value.
             */
            """.trimIndent(),
            mapOf("value" to ParamMetadata(typeName = "String", required = true, structural = ParamTypeInfo.Plain)),
        )

        assertTrue(result?.markdown?.contains("| `value`${KDocMarkdownParser.REQUIRED_MARKER} | `String` |  | The value.") == true)
    }

    @Test
    fun `marks a required param with the red asterisk marker`() {
        val result = KDocMarkdownParser.parse(
            """
            /**
             * @param label The label.
             */
            """.trimIndent(),
            mapOf("label" to ParamMetadata(typeName = "String", required = true, structural = ParamTypeInfo.Plain)),
        )

        assertTrue(result?.markdown?.contains("`label`${KDocMarkdownParser.REQUIRED_MARKER} | `String`") == true)
    }
}
