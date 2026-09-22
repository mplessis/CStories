package io.cstories.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

internal data class PreviewTheme(
    val isDark: Boolean,
    val wrapper: CStoriesThemeWrapper,
)

internal val LocalPreviewTheme = staticCompositionLocalOf<PreviewTheme?> { null }

@Composable
internal fun PreviewThemedContent(content: @Composable () -> Unit) {
    val previewTheme = LocalPreviewTheme.current
    if (previewTheme == null) {
        content()
    } else {
        previewTheme.wrapper(previewTheme.isDark, content)
    }
}
