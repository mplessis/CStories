package io.cstories.processor

import kotlin.test.Test
import kotlin.test.assertEquals

class ComponentRefsGeneratorTest {
    @Test
    fun `object name is fixed regardless of module name`() {
        assertEquals("CStoryComponentRefs", ComponentRefsGenerator.OBJECT_NAME)
    }

    @Test
    fun `qualified name includes the generated package`() {
        assertEquals(
            "io.cstories.generated.CStoryComponentRefs",
            ComponentRefsGenerator.QUALIFIED_NAME,
        )
    }
}
