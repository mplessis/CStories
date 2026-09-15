package io.cstories.runtime

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class CStoriesAppTest {
    @Test
    fun `clampSidebarWidth keeps values inside bounds`() {
        assertEquals(200.dp, clampSidebarWidth(120.dp, 900.dp))
        assertEquals(248.dp, clampSidebarWidth(248.dp, 900.dp))
        assertEquals(500.dp, clampSidebarWidth(500.dp, 900.dp))
    }

    @Test
    fun `clampSidebarWidth honors available width`() {
        assertEquals(200.dp, clampSidebarWidth(248.dp, 480.dp))
        assertEquals(260.dp, clampSidebarWidth(320.dp, 620.dp))
    }
}
