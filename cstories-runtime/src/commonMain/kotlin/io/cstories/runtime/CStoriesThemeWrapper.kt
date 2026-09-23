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
 * project supply its own wrapper. The generated catalog entry point can pass
 * it to [CStoriesApp], while individual stories can select an override.
 */
fun interface CStoriesThemeWrapper {
    @Composable
    operator fun invoke(isDark: Boolean, content: @Composable () -> Unit)
}

/**
 * Fallback used when no custom wrapper is passed to [CStoriesApp]: wraps the
 * story in a nested [MaterialTheme] using Material3's own dark/light color schemes.
 */
object DefaultCStoriesThemeWrapper : CStoriesThemeWrapper {
    @Composable
    override operator fun invoke(isDark: Boolean, content: @Composable () -> Unit) {
        MaterialTheme(colorScheme = if (isDark) darkColorScheme() else lightColorScheme()) {
            content()
        }
    }
}
