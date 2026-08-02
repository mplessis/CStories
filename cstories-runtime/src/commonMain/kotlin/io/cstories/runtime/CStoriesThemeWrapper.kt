package io.cstories.runtime

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Wraps the currently previewed story composable so it re-renders with a
 * dark or light appearance when the canvas theme switch (see
 * `StoryFrame.kt`) is toggled.
 *
 * `cstories-runtime` doesn't know about a consumer's own design system (e.g.
 * a `LumenTheme(isDark) { ... }`), so this indirection lets a consumer
 * project supply its own wrapper — see the `cstories { themeWrapper = ... }`
 * Gradle DSL wired by `cstories-gradle-plugin`, which injects a custom
 * reference into the generated entry point instead of [DefaultCStoriesThemeWrapper].
 */
typealias CStoriesThemeWrapper = @Composable (isDark: Boolean, content: @Composable () -> Unit) -> Unit

/**
 * Fallback used when no custom `cstories { themeWrapper = ... }` is
 * configured: wraps the story in a nested [MaterialTheme] using Material3's
 * own dark/light color schemes.
 */
val DefaultCStoriesThemeWrapper: CStoriesThemeWrapper = { isDark, content ->
    MaterialTheme(colorScheme = if (isDark) darkColorScheme() else lightColorScheme()) {
        content()
    }
}
