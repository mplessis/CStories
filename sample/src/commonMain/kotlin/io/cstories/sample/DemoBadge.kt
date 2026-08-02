package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A pill-shaped status badge used to highlight the state of an item.
 *
 * @param text The label displayed inside the badge.
 * @param tone The semantic tone controlling the badge's colors.
 */
@CStoryComponent
@Composable
fun DemoBadge(text: String, tone: BadgeTone) {
    val color = when (tone) {
        BadgeTone.Success -> Color(0xFF16A34A)
        BadgeTone.Warning -> Color(0xFFB45309)
        BadgeTone.Error -> Color(0xFFDC2626)
    }
    val background = when (tone) {
        BadgeTone.Success -> Color(0xFFDCFCE7)
        BadgeTone.Warning -> Color(0xFFFEF3C7)
        BadgeTone.Error -> Color(0xFFFEE2E2)
    }
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
