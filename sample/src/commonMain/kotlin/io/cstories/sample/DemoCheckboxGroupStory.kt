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
import io.cstories.runtime.knobs.IntKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

private val notificationChannels = listOf("Email", "Push", "SMS", "Slack", "Microsoft Teams", "Webhook")

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Checkbox group",
    component = CStoryComponentRefs.DemoCheckboxGroup,
)
@Composable
fun DemoCheckboxGroupStory() {
    var groupTitle by remember { mutableStateOf("Notify me by") }
    var optionCount by remember { mutableStateOf(4) }
    var selection by remember { mutableStateOf(setOf("Email")) }
    var hasSelectAll by remember { mutableStateOf(true) }
    var isEnabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = groupTitle, onValueChange = { groupTitle = it }, codeKey = "groupTitle")
        IntKnob(label = "Number of options", value = optionCount, onValueChange = { optionCount = it }, range = 2..notificationChannels.size)
        BooleanKnob(label = "Show \"Select all\"", value = hasSelectAll, onValueChange = { hasSelectAll = it }, codeKey = "hasSelectAll")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoCheckboxGroup(
        title = groupTitle,
        options = notificationChannels.take(optionCount),
        selected = selection,
        onSelectionChange = { selection = it },
        showSelectAll = hasSelectAll,
        enabled = isEnabled,
        modifier = Modifier.width(320.dp),
    )
}
