package io.cstories.sample

import androidx.compose.foundation.layout.width
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
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Feedback",
    name = "Empty state",
    component = CStoryComponentRefs.DemoEmptyState,
)
@Composable
fun DemoEmptyStateStory() {
    var emptyTitle by remember { mutableStateOf("No projects yet") }
    var emptyDescription by remember { mutableStateOf("Projects group your stories by product. Create one to get started.") }
    var selectedIcon by remember { mutableStateOf(SampleIcon.Inbox) }
    var showAction by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = emptyTitle, onValueChange = { emptyTitle = it }, codeKey = "emptyTitle")
        TextKnob(label = "Description", value = emptyDescription, onValueChange = { emptyDescription = it }, codeKey = "emptyDescription")
        SelectKnob(label = "Icon", value = selectedIcon, onValueChange = { selectedIcon = it }, codeKey = "selectedIcon")
        BooleanKnob(label = "Show action", value = showAction, onValueChange = { showAction = it })
    }

    DemoEmptyState(
        title = emptyTitle,
        description = emptyDescription,
        icon = selectedIcon.vector,
        modifier = Modifier.width(400.dp),
        action = if (showAction) {
            { DemoButton(text = "Create project", onClick = {}, leadingIcon = SampleIcon.Add.vector) }
        } else {
            null
        },
    )
}
