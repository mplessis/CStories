package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals

class StoryRegistryGeneratorTest {
    @Test
    fun `sanitizeModuleName converts separators to pascal case`() {
        assertEquals("DesignSystemCatalog", StoryRegistryGenerator.sanitizeModuleName("design-system_catalog"))
    }

    @Test
    fun `sanitizeModuleName falls back to default when blank`() {
        assertEquals("Default", StoryRegistryGenerator.sanitizeModuleName("  "))
    }
}
