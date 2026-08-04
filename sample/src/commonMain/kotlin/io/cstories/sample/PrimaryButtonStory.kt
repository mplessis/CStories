package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Primary",
    component = CStoryComponentRefs.PrimaryButton
)
@Composable
fun PrimaryButtonStory() {
    var label by remember { mutableStateOf("Click me") }
    var enabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(
            label = "Label",
            value = label,
            onValueChange = { label = it },
            codeKey = "label",
        )
        BooleanKnob(
            label = "Enabled",
            value = enabled,
            onValueChange = { enabled = it },
            codeKey = "enabled",
        )
    }

    PrimaryButton(
        text = label,
        enabled = enabled,
        onClick = {},
    )
}
