package io.cstories.runtime

import androidx.compose.runtime.Composable

data class StoryEntry(
    val path: List<String>,
    val composableInvoker: @Composable () -> Unit,
)
