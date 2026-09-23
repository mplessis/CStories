package io.cstories.sample

import androidx.compose.runtime.Composable
import io.cstories.runtime.CStoriesThemeWrapper
import io.cstories.runtime.DefaultCStoriesThemeWrapper

object CustomCStoriesThemeWrapper : CStoriesThemeWrapper {
    @Composable
    override operator fun invoke(isDark: Boolean, content: @Composable () -> Unit) {
        DefaultCStoriesThemeWrapper(isDark, content)
    }
}
