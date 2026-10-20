package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
    group = "DataDisplay",
    name = "Chip",
    component = CStoryComponentRefs.DemoChip,
)
@Composable
fun DemoChipStory() {
    var chipText by remember { mutableStateOf("Assigned to me") }
    var selectedType by remember { mutableStateOf(ChipType.Filter) }
    var isSelected by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf(SampleIcon.Person) }
    var isEnabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Text", value = chipText, onValueChange = { chipText = it }, codeKey = "chipText")
        SelectKnob(label = "Type", value = selectedType, onValueChange = { selectedType = it }, codeKey = "selectedType")
        BooleanKnob(label = "Selected", value = isSelected, onValueChange = { isSelected = it }, codeKey = "isSelected")
        SelectKnob(label = "Leading icon", value = selectedIcon, onValueChange = { selectedIcon = it }, codeKey = "selectedIcon")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoChip(
        text = chipText,
        type = selectedType,
        selected = isSelected,
        onClick = { isSelected = !isSelected },
        onRemove = {},
        leadingIcon = selectedIcon.vector,
        enabled = isEnabled,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "Recipients",
    component = CStoryComponentRefs.DemoChip,
)
@Composable
fun DemoChipRecipientsStory() {
    var recipients by remember { mutableStateOf(sampleMembers.take(5).map { it.first }) }

    FlowRow(
        modifier = Modifier.width(420.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        recipients.forEach { recipient ->
            DemoChip(
                text = recipient,
                type = ChipType.Input,
                onRemove = { recipients = recipients - recipient },
            )
        }
    }
}
