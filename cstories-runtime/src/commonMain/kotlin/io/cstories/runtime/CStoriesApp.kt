package io.cstories.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CStoriesApp(stories: List<StoryEntry>) {
    val tree = remember(stories) { buildTree(stories) }
    var selected by remember(stories) { mutableStateOf(stories.firstOrNull()) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                Sidebar(
                    tree = tree,
                    selectedPath = selected?.path,
                    onSelect = { selected = it },
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                )
                VerticalDivider()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    selected?.let { entry ->
                        StoryFrame(entry)
                    } ?: EmptyState()
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Text("No stories available")
}
