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
    name = "Alert",
    component = CStoryComponentRefs.DemoAlert,
)
@Composable
fun DemoAlertStory() {
    var alertTitle by remember { mutableStateOf("Your changes were saved") }
    var alertMessage by remember { mutableStateOf("The new settings apply to every member of the workspace.") }
    var selectedTone by remember { mutableStateOf(StatusTone.Success) }
    var isDismissible by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = alertTitle, onValueChange = { alertTitle = it }, codeKey = "alertTitle")
        TextKnob(label = "Message", value = alertMessage, onValueChange = { alertMessage = it }, codeKey = "alertMessage")
        SelectKnob(label = "Tone", value = selectedTone, onValueChange = { selectedTone = it }, codeKey = "selectedTone")
        BooleanKnob(label = "Dismissible", value = isDismissible, onValueChange = { isDismissible = it })
        BooleanKnob(label = "Visible", value = isVisible, onValueChange = { isVisible = it })
    }

    if (isVisible) {
        DemoAlert(
            title = alertTitle,
            message = alertMessage,
            tone = selectedTone,
            onDismiss = if (isDismissible) ({ isVisible = false }) else null,
            modifier = Modifier.width(420.dp),
        )
    }
}

@CStory(
    collection = "DesignSystem",
    group = "Feedback",
    name = "Alert with action",
    component = CStoryComponentRefs.DemoAlert,
)
@Composable
fun DemoAlertWithActionStory() {
    var actionLabel by remember { mutableStateOf("Retry") }

    KnobPanel {
        TextKnob(label = "Action label", value = actionLabel, onValueChange = { actionLabel = it }, codeKey = "actionLabel")
    }

    DemoAlert(
        title = "Upload failed",
        message = "The connection was interrupted while uploading report.pdf.",
        tone = StatusTone.Error,
        modifier = Modifier.width(420.dp),
        action = {
            DemoButton(text = actionLabel, onClick = {}, variant = ButtonVariant.Tertiary, size = ButtonSize.Small)
        },
    )
}
