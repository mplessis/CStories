package io.cstories.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeSectionsTest {
    @Test
    fun `summary is the first paragraph without inline markers`() {
        val documentation = """
            The design system's `button`, with **one** API
            for every variant.

            ### Usage
            - Keep one primary button per view.
        """.trimIndent()

        assertEquals(
            "The design system's button, with one API for every variant.",
            documentationSummary(documentation),
        )
    }

    @Test
    fun `summary is null without a leading paragraph`() {
        assertNull(documentationSummary(null))
        assertNull(documentationSummary("   "))
        assertNull(documentationSummary("**Parameters**\n\n| Name | Type |"))
        assertNull(documentationSummary("### Usage\n- Item"))
    }

    @Test
    fun `sections follow the sidebar order with one card per story group`() {
        val tree = buildTree(
            listOf(
                story("v1/Components/Forms/TextField", "Default", documentation = "A text input.\n\n### Usage"),
                story("v1/Components/Forms/TextField", "Password"),
                story("v1/Components/Forms/Checkbox", "Default"),
                story("v1/Components/Actions/Button", "Primary"),
                story("v1/Screens/Mobile", "Welcome"),
            ),
        )

        val sections = buildHomeSections(tree)

        assertEquals(
            listOf(
                listOf("v1", "Components", "Actions"),
                listOf("v1", "Components", "Forms"),
                listOf("v1", "Screens"),
            ),
            sections.map { it.path },
        )
        val forms = sections[1].cards
        assertEquals(listOf("Checkbox", "TextField"), forms.map { it.name })
        assertEquals(listOf(1, 2), forms.map { it.storyCount })
        assertEquals("A text input.", forms[1].summary)
        assertNull(forms[0].summary)
        assertEquals(listOf("v1", "Components", "Forms", "TextField", "Default"), forms[1].firstStory.path)
    }

    private fun story(group: String, name: String, documentation: String? = null): StoryEntry =
        StoryEntry(path = group.split('/') + name, composableInvoker = {}, documentation = documentation)
}
