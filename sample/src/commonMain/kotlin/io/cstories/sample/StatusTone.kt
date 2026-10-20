package io.cstories.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Semantic tone shared by status-carrying components such as [DemoBadge],
 * [DemoAlert] and [DemoStatCard].
 *
 * A tone conveys *meaning*, not a raw color: the actual colors are resolved
 * at composition time by [statusToneColors] so they follow the current
 * light/dark Material theme.
 */
enum class StatusTone {
    /** Neutral, informational state with no particular urgency. Uses the theme's primary container. */
    Info,

    /** Positive outcome: an operation succeeded or a metric improved. Rendered in green. */
    Success,

    /** Something needs attention but is not blocking. Rendered in amber. */
    Warning,

    /** Something failed or is blocking. Uses the theme's error colors. */
    Error,

    /** No semantic meaning, used for counters or plain labels. Uses the theme's surface variant. */
    Neutral,
}

/** Resolved foreground/background pair for a [StatusTone]. */
@Immutable
data class StatusToneColors(
    /** Background color of the container (badge pill, alert surface...). */
    val container: Color,
    /** Color of text and icons drawn on top of [container]. */
    val content: Color,
    /** Strong accent color, e.g. a status dot or an alert's leading stripe. */
    val accent: Color,
)

/**
 * Resolves [tone] to concrete colors for the current [MaterialTheme].
 *
 * Success and Warning have no Material 3 role, so they use fixed green and
 * amber palettes chosen according to the luminance of the theme's surface.
 */
@Composable
fun statusToneColors(tone: StatusTone): StatusToneColors {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.surface.luminance() < 0.5f
    return when (tone) {
        StatusTone.Info -> StatusToneColors(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.primary)
        StatusTone.Error -> StatusToneColors(scheme.errorContainer, scheme.onErrorContainer, scheme.error)
        StatusTone.Neutral -> StatusToneColors(scheme.surfaceVariant, scheme.onSurfaceVariant, scheme.outline)
        StatusTone.Success -> if (isDark) {
            StatusToneColors(Color(0xFF14532D), Color(0xFFBBF7D0), Color(0xFF4ADE80))
        } else {
            StatusToneColors(Color(0xFFDCFCE7), Color(0xFF166534), Color(0xFF16A34A))
        }
        StatusTone.Warning -> if (isDark) {
            StatusToneColors(Color(0xFF713F12), Color(0xFFFDE68A), Color(0xFFFBBF24))
        } else {
            StatusToneColors(Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFD97706))
        }
    }
}
