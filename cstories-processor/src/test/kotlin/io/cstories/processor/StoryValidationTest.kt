package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoryValidationTest {
    @Test
    fun `validateGroupAndName accepts valid values`() {
        assertNull(StoryValidation.validateGroupAndName("DesignSystem/Buttons", "Primary"))
    }

    @Test
    fun `validateGroupAndName rejects blank group`() {
        assertEquals(
            "@CStory group and name must not be blank",
            StoryValidation.validateGroupAndName("", "Primary"),
        )
    }

    @Test
    fun `validateGroupAndName rejects blank name`() {
        assertEquals(
            "@CStory group and name must not be blank",
            StoryValidation.validateGroupAndName("Buttons", " "),
        )
    }

    @Test
    fun `validateGroupAndName rejects slash in name`() {
        assertEquals(
            "@CStory 'name' must not contain '/', use 'group' for hierarchy: Primary/Default",
            StoryValidation.validateGroupAndName("Buttons", "Primary/Default"),
        )
    }

    @Test
    fun `validateGroupAndName rejects empty group segments`() {
        assertEquals(
            "@CStory 'group' must not contain empty segments: Buttons//Nested",
            StoryValidation.validateGroupAndName("Buttons//Nested", "Primary"),
        )
    }
}
