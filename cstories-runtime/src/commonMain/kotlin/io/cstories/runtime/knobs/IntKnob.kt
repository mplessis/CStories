package io.cstories.runtime.knobs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors
import kotlin.math.roundToInt

private const val MAX_VISIBLE_TICKS = 20

/**
 * A slider knob for integer values constrained to [range], snapping to
 * multiples of [step] from `range.first`. The current value is displayed
 * next to [label], and published as an `Int` literal under [codeKey].
 */
@Composable
fun IntKnob(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    codeKey: String? = null,
) {
    require(step > 0) { "step must be positive" }
    require(!range.isEmpty()) { "range must not be empty" }
    PublishKnobValue(codeKey, value)
    val intervals = (range.last - range.first) / step
    Knob(label = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = CStoriesColors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value.toString(),
                color = CStoriesColors.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Slider(
            value = value.coerceIn(range).toFloat(),
            onValueChange = { raw ->
                val snapped = range.first + ((raw - range.first) / step).roundToInt() * step
                val clamped = snapped.coerceIn(range.first, range.first + intervals * step)
                if (clamped != value) onValueChange(clamped)
            },
            valueRange = range.first.toFloat()..(range.first + intervals * step).toFloat(),
            // Tick marks become unreadable on wide ranges; snapping is handled in onValueChange.
            steps = if (intervals <= MAX_VISIBLE_TICKS) (intervals - 1).coerceAtLeast(0) else 0,
            colors = SliderDefaults.colors(
                thumbColor = CStoriesColors.primary,
                activeTrackColor = CStoriesColors.primary,
                inactiveTrackColor = CStoriesColors.borderSoft,
                activeTickColor = CStoriesColors.primary,
                inactiveTickColor = CStoriesColors.border,
            ),
        )
    }
}
