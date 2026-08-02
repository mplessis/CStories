package io.cstories.runtime.knobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors

/**
 * Publishes [value]'s Kotlin literal form (or [literalValue] verbatim if
 * provided, unquoted — e.g. for enum constants such as `BadgeTone.Success`)
 * under [codeKey] into [LocalKnobValuesSlot], so the story's "Code" tab can
 * substitute it into the usage snippet. A no-op when [codeKey] is `null` or
 * when rendered outside [io.cstories.runtime.CStoriesApp].
 */
@Composable
internal fun PublishKnobValue(codeKey: String?, value: Any?, literalValue: String? = null) {
    val slot = LocalKnobValuesSlot.current
    if (codeKey != null && slot != null) {
        val literal = literalValue ?: io.cstories.runtime.formatAsKotlinLiteral(value)
        SideEffect {
            slot.value = slot.value + (codeKey to literal)
        }
    }
}

/**
 * Shared layout for a single knob row: optional label, the control itself,
 * and a soft bottom divider, matching the mockup's `.knob` block.
 */
@Composable
internal fun Knob(label: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        if (label != null) {
            Text(
                text = label,
                color = CStoriesColors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        content()
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = CStoriesColors.borderSoft)
    }
}
