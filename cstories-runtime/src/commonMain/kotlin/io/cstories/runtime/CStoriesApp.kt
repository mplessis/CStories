package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.cstories.runtime.knobs.LocalControlsSlot
import io.cstories.runtime.knobs.LocalKnobValuesSlot
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.empty_state_no_stories
import org.jetbrains.compose.resources.stringResource

private val SidebarDefaultWidth = 248.dp
private val SidebarMinWidth = 200.dp
private val SidebarMaxWidth = 520.dp
private val SidebarResizeHandleWidth = 8.dp
private val SidebarContentMinWidth = 360.dp

internal fun maxSidebarWidth(availableWidth: Dp): Dp {
    val candidate = availableWidth - SidebarContentMinWidth
    return if (candidate < SidebarMinWidth) SidebarMinWidth else minOf(SidebarMaxWidth, candidate)
}

internal fun clampSidebarWidth(requestedWidth: Dp, availableWidth: Dp): Dp {
    return requestedWidth.coerceIn(SidebarMinWidth, maxSidebarWidth(availableWidth))
}

@Composable
fun CStoriesApp(stories: List<StoryEntry>, themeWrapper: CStoriesThemeWrapper = DefaultCStoriesThemeWrapper) {
    val tree = remember(stories) { buildTree(stories) }
    var selected by remember(stories) { mutableStateOf(stories.firstOrNull()) }
    val resetTokens = remember(stories) { mutableStateOf(mapOf<List<String>, Int>()) }
    var isCanvasDark by remember { mutableStateOf(false) }
    var canvasBackgroundStyle by remember { mutableStateOf(CanvasBackgroundStyle.Checkerboard) }
    var sidebarWidth by remember { mutableStateOf(SidebarDefaultWidth) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = CStoriesColors.pageBg) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val availableWidth = maxWidth
                val effectiveSidebarWidth = clampSidebarWidth(sidebarWidth, availableWidth)

                if (effectiveSidebarWidth != sidebarWidth) {
                    sidebarWidth = effectiveSidebarWidth
                }

                Row(Modifier.fillMaxSize()) {
                    Sidebar(
                        tree = tree,
                        selectedPath = selected?.path,
                        onSelect = { selected = it },
                        modifier = Modifier
                            .width(effectiveSidebarWidth)
                            .fillMaxHeight(),
                    )
                    SidebarResizeHandle(
                        sidebarWidth = effectiveSidebarWidth,
                        availableWidth = availableWidth,
                        onWidthChange = { sidebarWidth = it },
                    )
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
                                            backgroundStyle = canvasBackgroundStyle,
                                            onToggleBackgroundStyle = {
                                                canvasBackgroundStyle =
                                                    if (canvasBackgroundStyle == CanvasBackgroundStyle.Checkerboard) {
                                                        CanvasBackgroundStyle.Solid
                                                    } else {
                                                        CanvasBackgroundStyle.Checkerboard
                                                    }
                                            },
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
}

@Composable
private fun SidebarResizeHandle(
    sidebarWidth: Dp,
    availableWidth: Dp,
    onWidthChange: (Dp) -> Unit,
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val handleAlpha = if (isDragging || isHovered) 1f else 0.3f

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(SidebarResizeHandleWidth)
            .sidebarResizeCursor()
            .hoverable(interactionSource = interactionSource)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val deltaDp = with(density) { delta.toDp() }
                    onWidthChange(clampSidebarWidth(sidebarWidth + deltaDp, availableWidth))
                },
                onDragStarted = { isDragging = true },
                onDragStopped = { isDragging = false },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .alpha(handleAlpha)
                .background(if (isDragging) CStoriesColors.primary else CStoriesColors.borderSoft),
        )
        Box(
            modifier = Modifier
                .size(width = if (isDragging) 8.dp else 4.dp, height = 40.dp)
                .alpha(handleAlpha)
                .clip(RoundedCornerShape(2.dp))
                .background(CStoriesColors.primary),
        )
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(stringResource(Res.string.empty_state_no_stories), modifier = Modifier.padding(28.dp))
    }
}
