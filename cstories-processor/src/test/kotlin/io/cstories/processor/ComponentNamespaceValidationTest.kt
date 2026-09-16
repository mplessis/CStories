package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ComponentNamespaceValidationTest {
    @Test
    fun `accepts empty namespace for historical behavior`() {
        assertNull(ComponentNamespaceValidation.validate(""))
    }

    @Test
    fun `accepts valid kotlin identifier`() {
        assertNull(ComponentNamespaceValidation.validate("v2"))
    }

    @Test
    fun `rejects invalid characters`() {
        assertEquals(
            "Invalid @CStoryComponent namespace 'v-2': expected a single valid Kotlin identifier that is not a reserved keyword",
            ComponentNamespaceValidation.validate("v-2"),
        )
    }

    @Test
    fun `rejects identifiers starting with digit`() {
        assertEquals(
            "Invalid @CStoryComponent namespace '2v': expected a single valid Kotlin identifier that is not a reserved keyword",
            ComponentNamespaceValidation.validate("2v"),
        )
    }

    @Test
    fun `rejects reserved keywords`() {
        assertEquals(
            "Invalid @CStoryComponent namespace 'object': expected a single valid Kotlin identifier that is not a reserved keyword",
            ComponentNamespaceValidation.validate("object"),
        )
    }
}
