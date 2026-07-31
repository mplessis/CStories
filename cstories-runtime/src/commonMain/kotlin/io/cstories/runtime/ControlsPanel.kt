package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.controls_empty_state
import io.cstories.runtime.resources.controls_reset_button
import io.cstories.runtime.resources.controls_title
import org.jetbrains.compose.resources.stringResource

/**
 * The right-hand "Controls" card, mirroring the mockup's `.controls-panel`:
 * a header with a Reset action, followed by whichever knobs the currently
 * selected story registered via [io.cstories.runtime.knobs.KnobPanel].
 */
@Composable
fun ControlsPanel(
    controlsSlot: MutableState<(@Composable () -> Unit)?>,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg))
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.controls_title),
                color = CStoriesColors.textFaint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CStoriesColors.surface,
                    contentColor = CStoriesColors.textMuted,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CStoriesColors.border),
                shape = RoundedCornerShape(999.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(Res.string.controls_reset_button), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))
        val content = controlsSlot.value
        if (content != null) {
            content()
        } else {
            Text(
                text = stringResource(Res.string.controls_empty_state),
                color = CStoriesColors.textFaint,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}
