package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DemoBadge(text: String, tone: String) {
    val color = when (tone) {
        "success" -> Color(0xFF16A34A)
        "warning" -> Color(0xFFB45309)
        "error" -> Color(0xFFDC2626)
        else -> Color(0xFF16A34A)
    }
    val background = when (tone) {
        "success" -> Color(0xFFDCFCE7)
        "warning" -> Color(0xFFFEF3C7)
        "error" -> Color(0xFFFEE2E2)
        else -> Color(0xFFDCFCE7)
    }
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
