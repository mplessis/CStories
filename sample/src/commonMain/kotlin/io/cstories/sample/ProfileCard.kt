package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A summary card of a person combining a [DemoAvatar], a role [DemoBadge],
 * a short bio and two [DemoButton] actions. It shows how design-system
 * components compose into a higher-level pattern.
 *
 * ### Usage
 * - Use it in directories, team pages or as the content of a popover on a user mention.
 * - The primary action is the most likely next step ("Message"); the secondary
 *   one is optional ("View profile").
 * - The card has no fixed width; it looks best between 260dp and 360dp.
 *
 * ### Accessibility
 * - The name is marked as a heading, so screen-reader users can jump from card to card.
 * - Both actions are regular buttons reachable with the keyboard, in reading order.
 *
 * @param name Full name of the person, used for the avatar initials and the title.
 * @param role Role displayed in a badge under the name, e.g. "Admin".
 * @param modifier Modifier applied to the card. Use it to set the width.
 * @param bio Optional short description of the person, two or three lines at most.
 * @param presence Availability shown on the avatar, see [Presence].
 * @param roleTone Tone of the role badge, see [StatusTone].
 * @param primaryActionLabel Label of the filled button.
 * @param onPrimaryAction Called when the filled button is clicked.
 * @param secondaryActionLabel Label of the outlined button, or `null` to hide it.
 * @param onSecondaryAction Called when the outlined button is clicked.
 */
@CStoryComponent
@Composable
fun ProfileCard(
    name: String,
    role: String,
    modifier: Modifier = Modifier,
    bio: String? = null,
    presence: Presence = Presence.Hidden,
    roleTone: StatusTone = StatusTone.Info,
    primaryActionLabel: String = "Message",
    onPrimaryAction: () -> Unit = {},
    secondaryActionLabel: String? = "View profile",
    onSecondaryAction: () -> Unit = {},
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DemoAvatar(name = name, size = AvatarSize.Large, presence = presence)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                DemoBadge(text = role, tone = roleTone, size = BadgeSize.Small)
            }
            if (!bio.isNullOrBlank()) {
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (secondaryActionLabel != null) {
                    DemoButton(text = secondaryActionLabel, onClick = onSecondaryAction, variant = ButtonVariant.Secondary)
                }
                DemoButton(text = primaryActionLabel, onClick = onPrimaryAction, leadingIcon = SampleIcon.Send.vector)
            }
        }
    }
}
