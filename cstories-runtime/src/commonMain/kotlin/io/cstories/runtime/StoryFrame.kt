package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.canvas_live_preview
import org.jetbrains.compose.resources.stringResource

/**
 * The canvas card hosting the live preview of the currently selected story,
 * mirroring the mockup's `.canvas-card` / `.story-frame` / `.story-stage`.
 *
 * [resetToken] is bumped by the controls panel's reset action to force the
 * story composable to remount, reinitializing any internal `remember{}` knob
 * state back to its defaults.
 */
@Composable
fun StoryFrame(entry: StoryEntry, resetToken: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg)),
    ) {
        CanvasToolbar()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            key(entry.path, resetToken) {
                entry.composableInvoker()
            }
        }
    }
}

@Composable
private fun CanvasToolbar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CStoriesColors.success),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.canvas_live_preview),
                color = CStoriesColors.textMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        androidx.compose.material3.HorizontalDivider(color = CStoriesColors.borderSoft)
    }
}
