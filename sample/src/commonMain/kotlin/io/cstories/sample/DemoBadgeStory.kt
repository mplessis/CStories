package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Badges",
    name = "Playground",
    component = CStoryComponentRefs.DemoBadge,
)
@Composable
fun DemoBadgePlaygroundStory() {
    var label by remember { mutableStateOf("New") }
    var selectedTone by remember { mutableStateOf(StatusTone.Success) }
    var selectedSize by remember { mutableStateOf(BadgeSize.Medium) }
    var showDot by remember { mutableStateOf(false) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        SelectKnob(label = "Tone", value = selectedTone, onValueChange = { selectedTone = it }, codeKey = "selectedTone")
        SelectKnob(label = "Size", value = selectedSize, onValueChange = { selectedSize = it }, codeKey = "selectedSize")
        BooleanKnob(label = "With dot", value = showDot, onValueChange = { showDot = it }, codeKey = "showDot")
    }

    DemoBadge(
        text = label,
        tone = selectedTone,
        size = selectedSize,
        withDot = showDot,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Badges",
    name = "All tones",
    component = CStoryComponentRefs.DemoBadge,
)
@Composable
fun DemoBadgeAllTonesStory() {
    var showDot by remember { mutableStateOf(true) }

    KnobPanel {
        BooleanKnob(label = "With dot", value = showDot, onValueChange = { showDot = it }, codeKey = "showDot")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusTone.entries.forEach { tone ->
            DemoBadge(text = tone.name, tone = tone, withDot = showDot)
        }
    }
}
