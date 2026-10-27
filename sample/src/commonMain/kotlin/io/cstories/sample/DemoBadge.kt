package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/** Size of a [DemoBadge]. */
enum class BadgeSize(
    internal val horizontalPadding: Dp,
    internal val verticalPadding: Dp,
    internal val dotSize: Dp,
) {
    /** Compact badge for dense places such as list items or table cells. */
    Small(8.dp, 2.dp, 6.dp),

    /** Default badge, readable on its own next to a title. */
    Medium(12.dp, 4.dp, 8.dp),
}

/**
 * A pill-shaped label that highlights the status or category of an item,
 * e.g. "Active", "Draft" or "3 new".
 *
 * ### Usage
 * - Pick the [tone] from what the status *means* (see [StatusTone]), not from the color you want.
 * - Keep the [text] to one or two words; a badge is read at a glance.
 * - Enable [withDot] when the badge represents a live state ("Online", "Running").
 * - A badge is not interactive. Use a `DemoChip` for something the user can click or remove.
 *
 * ### Accessibility
 * - Color is never the only signal: the [text] must carry the meaning on its own.
 * - The status dot is decorative and is not announced by screen readers.
 *
 * @param text The label displayed inside the badge. Truncated with an ellipsis if it does not fit on one line.
 * @param modifier Modifier applied to the badge container.
 * @param tone The semantic tone controlling the badge colors.
 * @param size The badge size, see [BadgeSize].
 * @param withDot When `true`, draws a small status dot before the [text] in the tone's accent color.
 *
 * @see statusToneColors
 */
@CStoryComponent
@Composable
fun DemoBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    size: BadgeSize = BadgeSize.Medium,
    withDot: Boolean = false,
) {
    val colors = statusToneColors(tone)
    Row(
        modifier = modifier
            .background(colors.container, RoundedCornerShape(percent = 50))
            .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (withDot) {
            Box(Modifier.size(size.dotSize).background(colors.accent, CircleShape))
        }
        Text(
            text = text,
            color = colors.content,
            style = when (size) {
                BadgeSize.Small -> MaterialTheme.typography.labelSmall
                BadgeSize.Medium -> MaterialTheme.typography.labelMedium
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
