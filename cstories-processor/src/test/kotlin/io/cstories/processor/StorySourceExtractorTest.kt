package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StorySourceExtractorTest {

    @Test
    fun `extracts a simple single-line call`() {
        val body = """
            Column {
                PrimaryButton(text = "Click me", enabled = true, onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCall(body, "PrimaryButton")

        assertEquals("""PrimaryButton(text = "Click me", enabled = true, onClick = {})""", result)
    }

    @Test
    fun `extracts a multi-line call with a trailing lambda`() {
        val body = """
            Column {
                KnobPanel {
                    TextKnob(label = "Label", value = label, onValueChange = { label = it })
                }

                PrimaryButton(
                    text = label,
                    enabled = enabled,
                    onClick = {},
                )
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCall(body, "PrimaryButton")

        assertEquals(
            """
            PrimaryButton(
                    text = label,
                    enabled = enabled,
                    onClick = {},
                )
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `returns null when the component is not called in the body`() {
        val body = """
            Column {
                Text("Nothing to see here")
            }
        """.trimIndent()

        assertNull(StorySourceExtractor.extractCall(body, "PrimaryButton"))
    }

    @Test
    fun `does not match a function name that is only a substring of a longer identifier`() {
        val body = """
            Column {
                PrimaryButtonGroup(items = listOf())
            }
        """.trimIndent()

        assertNull(StorySourceExtractor.extractCall(body, "PrimaryButton"))
    }

    @Test
    fun `includes a simple object qualifier before the function name`() {
        val body = """
            Column {
                LumenButton.Primary(onClick = {}, label = label, enabled = enabled)
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCall(body, "Primary")

        assertEquals(
            """LumenButton.Primary(onClick = {}, label = label, enabled = enabled)""",
            result,
        )
    }

    @Test
    fun `includes a multi-segment qualifier chain before the function name`() {
        val body = """
            Column {
                design.LumenButton.Primary(onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCall(body, "Primary")

        assertEquals("""design.LumenButton.Primary(onClick = {})""", result)
    }

    @Test
    fun `picks the first call when the component is invoked more than once`() {
        val body = """
            Column {
                PrimaryButton(text = "First", onClick = {})
                PrimaryButton(text = "Second", onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCall(body, "PrimaryButton")

        assertEquals("""PrimaryButton(text = "First", onClick = {})""", result)
    }
}
