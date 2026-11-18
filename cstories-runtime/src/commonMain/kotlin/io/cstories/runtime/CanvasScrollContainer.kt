package io.cstories.runtime

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/** Whether the current story allows the catalog to scroll content larger than its preview area. */
internal val LocalCanvasScroll = staticCompositionLocalOf { true }

/**
 * Hosts story content so that it scrolls when larger than the available area, without ever giving it
 * unbounded constraints.
 *
 * The content is measured with `max(viewport, intrinsic size)`: smaller content keeps the viewport
 * constraints (alignment, `fillMaxSize`…), larger content gets exactly its intrinsic size and scrolls.
 * Content that does not support intrinsic measurements (`LazyColumn`, `Scaffold`, any `SubcomposeLayout`)
 * falls back to the viewport size and is never scrolled by the container.
 *
 * @param horizontalState enables horizontal scrolling when non-null.
 */
@Composable
internal fun CanvasScrollContainer(
    verticalState: ScrollState,
    horizontalState: ScrollState?,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    // Scroll modifiers relax the constraints of their child: the viewport is captured before them. Both layout
    // modifiers belong to the same node, so they are always measured together.
    val viewport = remember { ViewportHolder() }
    Box(modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    viewport.constraints = constraints
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .verticalScroll(verticalState)
                .then(if (horizontalState != null) Modifier.horizontalScroll(horizontalState) else Modifier)
                .layout { measurable, _ ->
                    val viewportConstraints = viewport.constraints
                    val size = canvasContentSize(
                        viewport = IntSize(
                            width = if (viewportConstraints.hasBoundedWidth) viewportConstraints.maxWidth else 0,
                            height = if (viewportConstraints.hasBoundedHeight) viewportConstraints.maxHeight else 0,
                        ),
                        horizontal = horizontalState != null || !viewportConstraints.hasBoundedWidth,
                        minIntrinsicWidth = { measurable.minIntrinsicWidth(Constraints.Infinity) },
                        maxIntrinsicHeight = { width -> measurable.maxIntrinsicHeight(width) },
                    )
                    val placeable = measurable.measure(Constraints.fixed(size.width, size.height))
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            content = content,
        )
        if (verticalState.maxValue > 0) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(verticalState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
        if (horizontalState != null && horizontalState.maxValue > 0) {
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalState),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            )
        }
    }
}

private class ViewportHolder {
    var constraints: Constraints = Constraints()
}

/**
 * Size given to the scrollable content: at least [viewport], grown to the content's intrinsic size.
 * Returns [viewport] when the content does not support intrinsic measurements (`SubcomposeLayout`).
 */
internal fun canvasContentSize(
    viewport: IntSize,
    horizontal: Boolean,
    minIntrinsicWidth: () -> Int,
    maxIntrinsicHeight: (width: Int) -> Int,
): IntSize {
    return try {
        val width = if (horizontal) maxOf(viewport.width, minIntrinsicWidth()) else viewport.width
        val height = maxOf(viewport.height, maxIntrinsicHeight(width))
        IntSize(width, height)
    } catch (_: IllegalStateException) {
        viewport
    }
}
