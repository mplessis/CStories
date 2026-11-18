package io.cstories.runtime

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals

class CanvasScrollContainerTest {
    private val viewport = IntSize(400, 300)

    @Test
    fun `smaller content keeps the viewport size`() {
        val size = canvasContentSize(viewport, horizontal = true, minIntrinsicWidth = { 100 }, maxIntrinsicHeight = { 50 })
        assertEquals(viewport, size)
    }

    @Test
    fun `taller content grows to its intrinsic height at the viewport width`() {
        var queriedWidth = -1
        val size = canvasContentSize(
            viewport,
            horizontal = true,
            minIntrinsicWidth = { 100 },
            maxIntrinsicHeight = { width -> queriedWidth = width; 1200 },
        )
        assertEquals(IntSize(400, 1200), size)
        assertEquals(400, queriedWidth)
    }

    @Test
    fun `wider content grows horizontally only when horizontal scroll is enabled`() {
        val horizontal = canvasContentSize(viewport, horizontal = true, minIntrinsicWidth = { 900 }, maxIntrinsicHeight = { 50 })
        val vertical = canvasContentSize(viewport, horizontal = false, minIntrinsicWidth = { 900 }, maxIntrinsicHeight = { 50 })
        assertEquals(IntSize(900, 300), horizontal)
        assertEquals(viewport, vertical)
    }

    @Test
    fun `content without intrinsics support falls back to the viewport`() {
        val size = canvasContentSize(
            viewport,
            horizontal = true,
            minIntrinsicWidth = { error("Asking for intrinsic measurements of SubcomposeLayout layouts is not supported") },
            maxIntrinsicHeight = { 1200 },
        )
        assertEquals(viewport, size)
    }
}
