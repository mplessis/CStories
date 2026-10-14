package io.cstories.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeNodeTest {
    @Test
    fun `buildTree returns empty list for no stories`() {
        assertTrue(buildTree(emptyList()).isEmpty())
    }

    @Test
    fun `buildTree groups entries hierarchically`() {
        val tree = buildTree(
            listOf(
                story("Buttons", "Primary"),
                story("Buttons", "Secondary"),
                story("Cards", "Elevated"),
            ),
        )

        assertEquals(2, tree.size)

        val buttons = tree[0] as TreeNode.Group
        assertEquals("Buttons", buttons.name)
        assertEquals(listOf("Primary", "Secondary"), buttons.children.map { it.name })

        val cards = tree[1] as TreeNode.Group
        assertEquals("Cards", cards.name)
        assertEquals(listOf("Elevated"), cards.children.map { it.name })
    }

    @Test
    fun `buildTree preserves nested groups`() {
        val tree = buildTree(
            listOf(
                story("DesignSystem/Buttons", "Primary"),
                story("DesignSystem/Buttons", "Secondary"),
            ),
        )

        val designSystem = tree.single() as TreeNode.Group
        val buttons = designSystem.children.single() as TreeNode.Group

        assertEquals("DesignSystem", designSystem.name)
        assertEquals("Buttons", buttons.name)
        assertEquals(listOf("Primary", "Secondary"), buttons.children.map { it.name })
    }

    @Test
    fun `filterTree retains all descendants when a top-level group matches`() {
        val tree = buildTree(
            listOf(
                story("DesignSystem/Buttons", "Primary"),
                story("DesignSystem/Buttons", "Secondary"),
                story("DesignSystem/Inputs", "Text"),
                story("Utilities", "Toggle"),
            ),
        )

        assertEquals(
            listOf(
                listOf("DesignSystem", "Buttons", "Primary"),
                listOf("DesignSystem", "Buttons", "Secondary"),
                listOf("DesignSystem", "Inputs", "Text"),
            ),
            storyPaths(filterTree(tree, "design")),
        )
    }

    @Test
    fun `filterTree retains all descendants when a nested group matches`() {
        val tree = buildTree(
            listOf(
                story("DesignSystem/Buttons", "Primary"),
                story("DesignSystem/Buttons", "Secondary"),
                story("DesignSystem/Inputs", "Text"),
                story("Utilities", "Toggle"),
            ),
        )

        assertEquals(
            listOf(
                listOf("DesignSystem", "Buttons", "Primary"),
                listOf("DesignSystem", "Buttons", "Secondary"),
            ),
            storyPaths(filterTree(tree, "but")),
        )
    }

    private fun storyPaths(tree: List<TreeNode>): List<List<String>> = tree.flatMap { node ->
        when (node) {
            is TreeNode.Group -> storyPaths(node.children)
            is TreeNode.Leaf -> listOf(node.entry.path)
        }
    }

    private fun story(group: String, name: String): StoryEntry {
        return StoryEntry(
            path = group.split('/') + name,
            composableInvoker = {},
        )
    }
}
