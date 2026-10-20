package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * An inline banner that tells the user about something that happened or
 * needs their attention, without interrupting their flow.
 *
 * ### Usage
 * - Place it at the top of the area it relates to (a form, a page, a card).
 * - Pick the [tone] from the meaning of the message: `Error` for failures, `Warning` for
 *   risks, `Success` for confirmations, `Info` for neutral information.
 * - Keep the [title] short; put details in the [message].
 * - Use [onDismiss] for messages the user can safely ignore. Errors that block the user
 *   should stay visible until the problem is fixed.
 * - For short confirmations that disappear on their own, use a snackbar instead.
 *
 * ### Accessibility
 * - The alert is a *polite live region*: screen readers announce it when it appears or changes.
 *   `Error` alerts use an *assertive* live region and interrupt the current announcement.
 * - Each tone has its own icon, so the meaning isn't conveyed by color only.
 * - The close button is labeled "Dismiss".
 *
 * @param title Short summary of the message, in bold.
 * @param modifier Modifier applied to the alert container.
 * @param message Optional details shown below the title.
 * @param tone Semantic tone controlling the colors and the icon. `Neutral` uses the `Info` icon.
 * @param onDismiss When not `null`, shows a close button that calls this lambda. The caller is responsible for hiding the alert.
 * @param action Optional slot for an action below the message, usually a small `Tertiary` `DemoButton` such as "Retry".
 */
@CStoryComponent
@Composable
fun DemoAlert(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    tone: StatusTone = StatusTone.Info,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = statusToneColors(tone)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container, MaterialTheme.shapes.medium)
            .semantics {
                liveRegion = if (tone == StatusTone.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
            }
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = if (onDismiss != null) 4.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = tone.icon(),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.padding(top = 2.dp).size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f).padding(top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = colors.content)
            if (!message.isNullOrBlank()) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.content)
            }
            action?.invoke()
        }
        if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = colors.content)
            }
        }
    }
}

private fun StatusTone.icon(): ImageVector = when (this) {
    StatusTone.Info, StatusTone.Neutral -> Icons.Filled.Info
    StatusTone.Success -> Icons.Filled.CheckCircle
    StatusTone.Warning -> Icons.Filled.Warning
    StatusTone.Error -> Icons.Filled.Error
}
