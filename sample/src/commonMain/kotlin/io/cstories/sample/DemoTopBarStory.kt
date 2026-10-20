package io.cstories.sample

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.IntKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

private val topBarActions = listOf(
    Icons.Filled.Search to "Search",
    Icons.Filled.Notifications to "Notifications",
    Icons.Filled.Settings to "Settings",
)

@CStory(
    collection = "DesignSystem",
    group = "Navigation",
    name = "Top bar",
    component = CStoryComponentRefs.DemoTopBar,
)
@Composable
fun DemoTopBarStory() {
    var screenTitle by remember { mutableStateOf("Project settings") }
    var screenSubtitle by remember { mutableStateOf("CStories") }
    var showBack by remember { mutableStateOf(true) }
    var actionCount by remember { mutableStateOf(2) }

    KnobPanel {
        TextKnob(label = "Title", value = screenTitle, onValueChange = { screenTitle = it }, codeKey = "screenTitle")
        TextKnob(label = "Subtitle", value = screenSubtitle, onValueChange = { screenSubtitle = it }, codeKey = "screenSubtitle")
        BooleanKnob(label = "Back button", value = showBack, onValueChange = { showBack = it })
        IntKnob(label = "Number of actions", value = actionCount, onValueChange = { actionCount = it }, range = 0..topBarActions.size)
    }

    DemoTopBar(
        title = screenTitle,
        subtitle = screenSubtitle,
        onBack = if (showBack) ({}) else null,
        modifier = Modifier.width(480.dp),
        actions = {
            topBarActions.take(actionCount).forEach { (icon, description) ->
                IconButton(onClick = {}) {
                    Icon(icon, contentDescription = description)
                }
            }
        },
    )
}
