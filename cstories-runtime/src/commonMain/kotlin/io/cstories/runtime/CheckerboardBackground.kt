package io.cstories.runtime

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws a two-tone checkerboard grid behind the content, mimicking Photoshop's
 * transparency pattern. Used as the canvas stage backdrop in [StoryFrame] so
 * light/dark preview switches read clearly against a neutral, textured
 * background instead of a flat fill.
 */
fun Modifier.checkerboardBackground(
    cellSize: Dp = 8.dp,
    colorA: Color,
    colorB: Color,
): Modifier = drawBehind {
    val cellPx = cellSize.toPx()
    if (cellPx <= 0f) return@drawBehind
    var row = 0
    var y = 0f
    while (y < size.height) {
        var col = 0
        var x = 0f
        while (x < size.width) {
            val color = if ((row + col) % 2 == 0) colorA else colorB
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(
                    width = minOf(cellPx, size.width - x),
                    height = minOf(cellPx, size.height - y),
                ),
            )
            x += cellPx
            col++
        }
        y += cellPx
        row++
    }
}
