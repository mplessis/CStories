package io.cstories.runtime

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Renders a story's full path (collection / group.. / name) with the last
 * segment highlighted, mirroring the mockup's breadcrumb subtitle.
 */
@Composable
fun Breadcrumb(path: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        path.forEachIndexed { index, segment ->
            val isLast = index == path.lastIndex
            Text(
                text = segment,
                color = if (isLast) CStoriesColors.primary else CStoriesColors.textFaint,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.5.sp,
            )
            if (!isLast) {
                Text(
                    text = "  /  ",
                    color = CStoriesColors.textFaint,
                    fontSize = 12.5.sp,
                )
            }
        }
    }
}
