package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.annotations.CStoryComponent

/** Size of a [DemoAvatar]. */
enum class AvatarSize(internal val size: Dp, internal val presenceSize: Dp) {
    /** 24dp. For inline mentions and dense lists. */
    ExtraSmall(24.dp, 8.dp),

    /** 32dp. For comments and compact list items. */
    Small(32.dp, 10.dp),

    /** 40dp. The default size for list items. */
    Medium(40.dp, 12.dp),

    /** 64dp. For profile headers and cards. */
    Large(64.dp, 16.dp),
}

/** Outline of a [DemoAvatar]. */
enum class AvatarShape {
    /** Circle. Use it for people. */
    Circle,

    /** Rounded square. Use it for organizations, teams or projects. */
    Rounded,
}

/** Availability status displayed as a dot on a [DemoAvatar]. */
enum class Presence {
    /** No presence dot is shown. */
    Hidden,

    /** The person is active. Shown as a green dot. */
    Online,

    /** The person is idle. Shown as an amber dot. */
    Away,

    /** The person is not connected. Shown as a grey dot. */
    Offline,
}

/**
 * A visual identity for a person or an entity, rendered as initials on a
 * colored background, with an optional presence indicator.
 *
 * ### Usage
 * - Pass the full [name]: the initials (up to two letters) are computed from it,
 *   e.g. "Ada Lovelace" → "AL", "plato" → "P".
 * - The background color is derived from the [name], so the same person always gets the same color.
 * - Use [AvatarShape.Circle] for people and [AvatarShape.Rounded] for organizations.
 * - Show a [presence] only in real-time contexts such as chat.
 *
 * ### Accessibility
 * - The avatar is announced with the [name] and, when visible, the presence, e.g. "Ada Lovelace, Online".
 *   Hide it from accessibility in the caller if the name is already displayed next to it.
 *
 * @param name Full name of the person or entity. Used for the initials, the color and the accessible label.
 * @param modifier Modifier applied to the avatar.
 * @param size Diameter of the avatar, see [AvatarSize].
 * @param shape Outline of the avatar, see [AvatarShape].
 * @param presence Availability dot drawn at the bottom-right corner, see [Presence].
 */
@CStoryComponent
@Composable
fun DemoAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium,
    shape: AvatarShape = AvatarShape.Circle,
    presence: Presence = Presence.Hidden,
) {
    val outline: Shape = when (shape) {
        AvatarShape.Circle -> CircleShape
        AvatarShape.Rounded -> RoundedCornerShape(size.size / 4)
    }
    val background = avatarPalette[name.hashCode().mod(avatarPalette.size)]
    val accessibleLabel = if (presence == Presence.Hidden) name else "$name, ${presence.name}"

    Box(modifier = modifier.size(size.size).semantics { contentDescription = accessibleLabel }) {
        Box(
            modifier = Modifier.size(size.size).background(background, outline),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initialsOf(name),
                color = Color.White,
                fontSize = (size.size.value * 0.4f).sp,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (presence != Presence.Hidden) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size.presenceSize)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(presence.color(), CircleShape),
            )
        }
    }
}

/** Computes up to two uppercase initials from the first and last words of [name]. */
internal fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when (words.size) {
        0 -> "?"
        1 -> words[0].take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

private val avatarPalette = listOf(
    Color(0xFF6366F1),
    Color(0xFF0EA5E9),
    Color(0xFF14B8A6),
    Color(0xFFF59E0B),
    Color(0xFFEC4899),
    Color(0xFF8B5CF6),
)

@Composable
private fun Presence.color(): Color = when (this) {
    Presence.Online -> statusToneColors(StatusTone.Success).accent
    Presence.Away -> statusToneColors(StatusTone.Warning).accent
    Presence.Offline, Presence.Hidden -> MaterialTheme.colorScheme.outline
}
