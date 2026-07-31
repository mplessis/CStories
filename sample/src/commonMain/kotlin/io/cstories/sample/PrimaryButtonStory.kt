package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(group = "Button", name = "Primary")
@Composable
fun PrimaryButtonStory() {
    var label by remember { mutableStateOf("Click me") }
    var enabled by remember { mutableStateOf(true) }

    Column {
        KnobPanel {
            TextKnob(
                label = "Label",
                value = label,
                onValueChange = { label = it },
            )
            BooleanKnob(
                label = "Enabled",
                value = enabled,
                onValueChange = { enabled = it },
            )
        }

        PrimaryButton(
            text = label,
            enabled = enabled,
            onClick = {},
        )
    }
}
