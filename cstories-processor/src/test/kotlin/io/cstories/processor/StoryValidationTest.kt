package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoryValidationTest {
    @Test
    fun `validateCollectionGroupAndName accepts valid values`() {
        assertNull(StoryValidation.validateCollectionGroupAndName("DesignSystem", "Buttons", "Primary"))
    }

    @Test
    fun `validateCollectionGroupAndName rejects blank collection`() {
        assertEquals(
            "@CStory collection, group and name must not be blank",
            StoryValidation.validateCollectionGroupAndName("", "Buttons", "Primary"),
        )
    }

    @Test
    fun `validateCollectionGroupAndName rejects blank group`() {
        assertEquals(
            "@CStory collection, group and name must not be blank",
            StoryValidation.validateCollectionGroupAndName("DesignSystem", "", "Primary"),
        )
    }

    @Test
    fun `validateCollectionGroupAndName rejects blank name`() {
        assertEquals(
            "@CStory collection, group and name must not be blank",
            StoryValidation.validateCollectionGroupAndName("DesignSystem", "Buttons", " "),
        )
    }

    @Test
    fun `validateCollectionGroupAndName rejects slash in name`() {
        assertEquals(
            "@CStory 'name' must not contain '/', use 'group' for hierarchy: Primary/Default",
            StoryValidation.validateCollectionGroupAndName("DesignSystem", "Buttons", "Primary/Default"),
        )
    }

    @Test
    fun `validateCollectionGroupAndName rejects empty collection segments`() {
        assertEquals(
            "@CStory 'collection' must not contain empty segments: DesignSystem//Nested",
            StoryValidation.validateCollectionGroupAndName("DesignSystem//Nested", "Buttons", "Primary"),
        )
    }

    @Test
    fun `validateCollectionGroupAndName rejects empty group segments`() {
        assertEquals(
            "@CStory 'group' must not contain empty segments: Buttons//Nested",
            StoryValidation.validateCollectionGroupAndName("DesignSystem", "Buttons//Nested", "Primary"),
        )
    }
}
