package io.cstories.runtime.knobs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors

@Composable
fun BooleanKnob(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    codeKey: String? = null,
) {
    PublishKnobValue(codeKey, value)
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
            Switch(
                checked = value,
                onCheckedChange = onValueChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = CStoriesColors.primary,
                ),
            )
        }
    }
}
