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

        val result = StorySourceExtractor.extractCalls(body, "PrimaryButton").firstOrNull()

        assertEquals("""PrimaryButton(text = "Click me", enabled = true, onClick = {})""", result)
    }

    @Test
    fun `extracts a multi-line call re-indented relative to its start column`() {
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

        val result = StorySourceExtractor.extractCalls(body, "PrimaryButton").firstOrNull()

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

        assertNull(StorySourceExtractor.extractCalls(body, "PrimaryButton").firstOrNull())
    }

    @Test
    fun `does not match a function name that is only a substring of a longer identifier`() {
        val body = """
            Column {
                PrimaryButtonGroup(items = listOf())
            }
        """.trimIndent()

        assertNull(StorySourceExtractor.extractCalls(body, "PrimaryButton").firstOrNull())
    }

    @Test
    fun `includes a simple object qualifier before the function name`() {
        val body = """
            Column {
                DemoButton.Primary(onClick = {}, label = label, enabled = enabled)
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCalls(body, "Primary").firstOrNull()

        assertEquals(
            """DemoButton.Primary(onClick = {}, label = label, enabled = enabled)""",
            result,
        )
    }

    @Test
    fun `includes a multi-segment qualifier chain before the function name`() {
        val body = """
            Column {
                design.DemoButton.Primary(onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCalls(body, "Primary").firstOrNull()

        assertEquals("""design.DemoButton.Primary(onClick = {})""", result)
    }

    @Test
    fun `extracts every call in source order`() {
        val body = """
            Column {
                PrimaryButton(text = "First", onClick = {})
                PrimaryButton(text = "Second", onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCalls(body, "PrimaryButton")

        assertEquals(
            listOf(
                """PrimaryButton(text = "First", onClick = {})""",
                """PrimaryButton(text = "Second", onClick = {})""",
            ),
            result,
        )
    }

    @Test
    fun `does not extract a call nested inside an already extracted call again`() {
        val body = """
            Card(
                content = {
                    Card(title = "Inner")
                },
            )
        """.trimIndent()

        val result = StorySourceExtractor.extractCalls(body, "Card")

        assertEquals(1, result.size)
        assertEquals(
            """
            Card(
                content = {
                    Card(title = "Inner")
                },
            )
            """.trimIndent(),
            result.single(),
        )
    }

    @Test
    fun `re-indents each call found inside a nested layout`() {
        val body = """
            Row {
                items.forEach { item ->
                    PrimaryButton(
                        text = item,
                        onClick = {},
                    )
                }
                PrimaryButton(text = "Last", onClick = {})
            }
        """.trimIndent()

        val result = StorySourceExtractor.extractCalls(body, "PrimaryButton")

        assertEquals(
            listOf(
                """
                PrimaryButton(
                    text = item,
                    onClick = {},
                )
                """.trimIndent(),
                """PrimaryButton(text = "Last", onClick = {})""",
            ),
            result,
        )
    }

    @Test
    fun `returns an empty list when the component is never called`() {
        val body = """
            Column {
                Text("Nothing to see here")
            }
        """.trimIndent()

        assertEquals(emptyList(), StorySourceExtractor.extractCalls(body, "PrimaryButton"))
    }
}
