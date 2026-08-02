package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.runtime.knobs.LocalControlsSlot
import io.cstories.runtime.knobs.LocalKnobValuesSlot
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.empty_state_no_stories
import org.jetbrains.compose.resources.stringResource

@Composable
fun CStoriesApp(stories: List<StoryEntry>, themeWrapper: CStoriesThemeWrapper = DefaultCStoriesThemeWrapper) {
    val tree = remember(stories) { buildTree(stories) }
    var selected by remember(stories) { mutableStateOf(stories.firstOrNull()) }
    val resetTokens = remember(stories) { mutableStateOf(mapOf<List<String>, Int>()) }
    var isCanvasDark by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = CStoriesColors.pageBg) {
            Row(Modifier.fillMaxSize()) {
                Sidebar(
                    tree = tree,
                    selectedPath = selected?.path,
                    onSelect = { selected = it },
                    modifier = Modifier
                        .width(248.dp)
                        .fillMaxHeight(),
                )
                VerticalDivider(color = CStoriesColors.borderSoft)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(CStoriesColors.surfaceMuted),
                ) {
                    selected?.let { entry ->
                        val controlsSlot = remember(entry.path) {
                            mutableStateOf<(@Composable () -> Unit)?>(null)
                        }
                        val resetToken = resetTokens.value[entry.path] ?: 0
                        val knobValuesSlot = remember(entry.path, resetToken) {
                            mutableStateOf<Map<String, String>>(emptyMap())
                        }
                        MainHeader(breadcrumbPath = entry.path)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                        ) {
                            CompositionLocalProvider(
                                LocalControlsSlot provides controlsSlot,
                                LocalKnobValuesSlot provides knobValuesSlot,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                ) {
                                    StoryFrame(
                                        entry = entry,
                                        resetToken = resetToken,
                                        isDark = isCanvasDark,
                                        onToggleDark = { isCanvasDark = !isCanvasDark },
                                        themeWrapper = themeWrapper,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                    )
                                    if (entry.documentation != null || entry.usageCode != null) {
                                        DocsCodeTabs(
                                            documentation = entry.documentation,
                                            usageCode = entry.usageCode,
                                            knobValues = knobValuesSlot.value,
                                        )
                                    }
                                }
                                Box(modifier = Modifier.width(20.dp))
                                ControlsPanel(
                                    controlsSlot = controlsSlot,
                                    onReset = {
                                        resetTokens.value = resetTokens.value +
                                            (entry.path to ((resetTokens.value[entry.path] ?: 0) + 1))
                                    },
                                    modifier = Modifier
                                        .width(288.dp)
                                        .fillMaxHeight(),
                                )
                            }
                        }
                    } ?: EmptyState()
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(stringResource(Res.string.empty_state_no_stories), modifier = Modifier.padding(28.dp))
    }
}
