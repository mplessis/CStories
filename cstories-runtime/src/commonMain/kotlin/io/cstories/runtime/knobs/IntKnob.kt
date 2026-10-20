package io.cstories.runtime.knobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors
import kotlin.math.roundToInt

/**
 * A slider knob for integer values constrained to [range], snapping to
 * multiples of [step] from `range.first`. The current value is displayed
 * next to [label], and published as an `Int` literal under [codeKey].
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val lastValue = range.first + intervals * step
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
        // Snapping is done here rather than with `steps`, which would draw tick marks.
        Slider(
            value = value.coerceIn(range.first, lastValue).toFloat(),
            onValueChange = { raw ->
                val snapped = range.first + ((raw - range.first) / step).roundToInt() * step
                val clamped = snapped.coerceIn(range.first, lastValue)
                if (clamped != value) onValueChange(clamped)
            },
            valueRange = range.first.toFloat()..lastValue.toFloat(),
            modifier = Modifier.fillMaxWidth().height(24.dp),
            thumb = { IntKnobThumb() },
            track = { state -> IntKnobTrack(state) },
        )
    }
}

@Composable
private fun IntKnobThumb() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(CStoriesColors.primary, CircleShape)
            .border(2.dp, Color.White, CircleShape),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntKnobTrack(state: SliderState) {
    val span = state.valueRange.endInclusive - state.valueRange.start
    val fraction = if (span > 0f) ((state.value - state.valueRange.start) / span).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(CStoriesColors.border, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(CStoriesColors.primary, CircleShape),
        )
    }
}
