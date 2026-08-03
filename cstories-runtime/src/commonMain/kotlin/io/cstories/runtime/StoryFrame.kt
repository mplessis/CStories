package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.canvas_live_preview
import io.cstories.runtime.resources.canvas_theme_toggle_to_dark
import io.cstories.runtime.resources.canvas_theme_toggle_to_light
import org.jetbrains.compose.resources.stringResource

/**
 * The canvas card hosting the live preview of the currently selected story,
 * mirroring the mockup's `.canvas-card` / `.story-frame` / `.story-stage`.
 *
 * [resetToken] is bumped by the controls panel's reset action to force the
 * story composable to remount, reinitializing any internal `remember{}` knob
 * state back to its defaults.
 *
 * [isDark] toggles the background of the preview stage AND passes through
 * [themeWrapper], which wraps the story composable so components relying
 * on the consumer's own design system (or [DefaultCStoriesThemeWrapper]'s
 * Material3 `colorScheme`) actually re-render with dark colors rather than
 * just sitting on a dark backdrop.
 */
@Composable
fun StoryFrame(
    entry: StoryEntry,
    resetToken: Int,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    themeWrapper: CStoriesThemeWrapper,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg)),
    ) {
        CanvasToolbar(isDark = isDark, onToggleDark = onToggleDark)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 1.dp, bottom = 3.dp, end = 1.dp)
                .clip(RoundedCornerShape(bottomStart = CStoriesRadii.md, bottomEnd = CStoriesRadii.md))
                .checkerboardBackground(
                    colorA = if (isDark) CStoriesColors.checkerDarkA else CStoriesColors.checkerLightA,
                    colorB = if (isDark) CStoriesColors.checkerDarkB else CStoriesColors.checkerLightB,
                )
                .padding(if (isDark) 24.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            themeWrapper(isDark) {
                key(entry.path, resetToken) {
                    entry.composableInvoker()
                }
            }
        }
    }
}

@Composable
private fun CanvasToolbar(isDark: Boolean, onToggleDark: () -> Unit) {
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
            Spacer(Modifier.weight(1f))
            ThemeSwitch(isDark = isDark, onDarkChange = { dark -> if (dark != isDark) onToggleDark() })
        }
        androidx.compose.material3.HorizontalDivider(color = CStoriesColors.borderSoft)
    }
}

/**
 * Two-sided light/dark pill switch for the canvas preview background: the
 * currently active side is highlighted, and tapping either side selects it
 * (rather than a single toggle button that only communicates one state).
 */
@Composable
private fun ThemeSwitch(isDark: Boolean, onDarkChange: (Boolean) -> Unit) {
    val lightDescription = stringResource(Res.string.canvas_theme_toggle_to_light)
    val darkDescription = stringResource(Res.string.canvas_theme_toggle_to_dark)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .background(CStoriesColors.surfaceSunken)
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.sm))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemeSwitchSide(
            selected = !isDark,
            onClick = { onDarkChange(false) },
            contentDescription = lightDescription,
        ) {
            SunMoonIcon(
                isDark = false,
                tint = if (!isDark) androidx.compose.ui.graphics.Color.White else CStoriesColors.textFaint,
            )
        }
        ThemeSwitchSide(
            selected = isDark,
            onClick = { onDarkChange(true) },
            contentDescription = darkDescription,
        ) {
            SunMoonIcon(
                isDark = true,
                tint = if (isDark) androidx.compose.ui.graphics.Color.White else CStoriesColors.textFaint,
            )
        }
    }
}

@Composable
private fun ThemeSwitchSide(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CStoriesRadii.sm - 2.dp))
            .background(if (selected) CStoriesColors.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
