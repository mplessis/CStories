package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Badges",
    name = "Default",
    component = CStoryComponentRefs.DemoBadge,
)
@Composable
fun BadgeStory() {
    var text by remember { mutableStateOf("Nouveau") }
    var tone by remember { mutableStateOf(BadgeTone.Success) }

    Column {
        KnobPanel {
            TextKnob(label = "Text", value = text, onValueChange = { text = it }, codeKey = "text")
            SelectKnob(
                label = "Tone",
                value = tone,
                onValueChange = { tone = it },
                codeKey = "tone",
            )
        }

        DemoBadge(text = text, tone = tone)
    }
}
