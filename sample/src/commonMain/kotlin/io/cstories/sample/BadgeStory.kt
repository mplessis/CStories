package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(collection = "DesignSystem", group = "Badges", name = "Default")
@Composable
fun BadgeStory() {
    var text by remember { mutableStateOf("Nouveau") }
    var tone by remember { mutableStateOf("success") }

    Column {
        KnobPanel {
            TextKnob(label = "Text", value = text, onValueChange = { text = it })
            SelectKnob(
                label = "Tone",
                value = tone,
                options = listOf("success", "warning", "error"),
                onValueChange = { tone = it },
            )
        }

        DemoBadge(text = text, tone = tone)
    }
}
