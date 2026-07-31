package io.cstories.runtime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sidebar hierarchy icons, redrawn faithfully from the inline SVGs used in
 * the design mockup (design/version1 - ok.html): a 2x2 grid glyph for the
 * top-level collection row, and a "layers" glyph for nested groups.
 */

/** Mirrors the root-swatch SVG: three stroked rounded squares plus one filled square. */
@Composable
fun CollectionIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 13.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.width / 24f
        val strokeWidth = 1.6f * unit
        val cellSize = Size(8f * unit, 8f * unit)
        val corner = CornerRadius(2f * unit, 2f * unit)
        val stroke = Stroke(width = strokeWidth)

        drawRoundRect(
            color = tint,
            topLeft = Offset(3f * unit, 3f * unit),
            size = cellSize,
            cornerRadius = corner,
            style = stroke,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(13f * unit, 3f * unit),
            size = cellSize,
            cornerRadius = corner,
            style = stroke,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(3f * unit, 13f * unit),
            size = cellSize,
            cornerRadius = corner,
            style = stroke,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(13f * unit, 13f * unit),
            size = cellSize,
            cornerRadius = corner,
        )
    }
}

/** Mirrors the group-swatch SVG: a diamond cap over two stacked chevron "layers". */
@Composable
fun GroupIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 15.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.width / 24f
        val strokeWidth = 1.6f * unit

        val diamond = Path().apply {
            moveTo(12f * unit, 3f * unit)
            lineTo(21f * unit, 8f * unit)
            lineTo(12f * unit, 13f * unit)
            lineTo(3f * unit, 8f * unit)
            close()
        }
        drawPath(path = diamond, color = tint.copy(alpha = 0.35f))
        drawPath(
            path = diamond,
            color = tint,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
        )

        val midLayer = Path().apply {
            moveTo(3f * unit, 12f * unit)
            lineTo(12f * unit, 17f * unit)
            lineTo(21f * unit, 12f * unit)
        }
        drawPath(
            path = midLayer,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val bottomLayer = Path().apply {
            moveTo(3f * unit, 16f * unit)
            lineTo(12f * unit, 21f * unit)
            lineTo(21f * unit, 16f * unit)
        }
        drawPath(
            path = bottomLayer,
            color = tint.copy(alpha = 0.5f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/**
 * Drawn triangle chevron (rather than relying on a text glyph, which is
 * missing from some fonts on certain platforms and renders as a blank box).
 * Points down when [expanded], right otherwise.
 */
@Composable
fun ChevronIcon(
    expanded: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            if (expanded) {
                // pointing down
                moveTo(w * 0.15f, h * 0.35f)
                lineTo(w * 0.85f, h * 0.35f)
                lineTo(w * 0.5f, h * 0.75f)
                close()
            } else {
                // pointing right
                moveTo(w * 0.35f, h * 0.15f)
                lineTo(w * 0.75f, h * 0.5f)
                lineTo(w * 0.35f, h * 0.85f)
                close()
            }
        }
        drawPath(path = path, color = tint)
    }
}
