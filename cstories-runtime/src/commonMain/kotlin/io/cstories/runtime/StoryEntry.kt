package io.cstories.runtime

import androidx.compose.runtime.Composable

data class StoryEntry(
    val path: List<String>,
    val composableInvoker: @Composable () -> Unit,
    val themeWrapper: CStoriesThemeWrapper? = null,
    val documentation: String? = null,
    val usageCode: String? = null,
    /** `false` when the story opted out of canvas scrolling with `@CStory(canvasScroll = CanvasScroll.None)`. */
    val canvasScroll: Boolean = true,
)
