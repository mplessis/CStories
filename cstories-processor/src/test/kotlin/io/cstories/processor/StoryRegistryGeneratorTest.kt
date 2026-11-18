package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryRegistryGeneratorTest {
    @Test
    fun `sanitizeModuleName converts separators to pascal case`() {
        assertEquals("DesignSystemCatalog", StoryRegistryGenerator.sanitizeModuleName("design-system_catalog"))
    }

    @Test
    fun `sanitizeModuleName falls back to default when blank`() {
        assertEquals("Default", StoryRegistryGenerator.sanitizeModuleName("  "))
    }

    @Test
    fun `canvasScroll is emitted only when disabled`() {
        val auto = story(name = "Auto")
        val none = story(name = "None").copy(canvasScroll = false)

        val autoCode = StoryRegistryGenerator.buildEntriesInitializer(listOf(auto)).toString()
        val noneCode = StoryRegistryGenerator.buildEntriesInitializer(listOf(none)).toString()

        assertFalse("canvasScroll" in autoCode)
        assertTrue("canvasScroll = false" in noneCode)
    }

    private fun story(name: String) = StoryDescriptor(
        collection = "Collection",
        group = "Group",
        name = name,
        invoker = StoryInvoker.TopLevel(packageName = "sample", functionName = "Story"),
    )
}
