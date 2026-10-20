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
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Text field",
    component = CStoryComponentRefs.DemoTextField,
)
@Composable
fun DemoTextFieldStory() {
    var fieldValue by remember { mutableStateOf("") }
    var fieldLabel by remember { mutableStateOf("Email") }
    var fieldPlaceholder by remember { mutableStateOf("jane@example.com") }
    var helperText by remember { mutableStateOf("We'll never share your email.") }
    var selectedIcon by remember { mutableStateOf(SampleIcon.None) }
    var isEnabled by remember { mutableStateOf(true) }
    var characterLimit by remember { mutableStateOf(0) }

    KnobPanel {
        TextKnob(label = "Label", value = fieldLabel, onValueChange = { fieldLabel = it }, codeKey = "fieldLabel")
        TextKnob(label = "Placeholder", value = fieldPlaceholder, onValueChange = { fieldPlaceholder = it }, codeKey = "fieldPlaceholder")
        TextKnob(label = "Supporting text", value = helperText, onValueChange = { helperText = it }, codeKey = "helperText")
        SelectKnob(label = "Leading icon", value = selectedIcon, onValueChange = { selectedIcon = it }, codeKey = "selectedIcon")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
        IntKnob(label = "Max length (0 = none)", value = characterLimit, onValueChange = { characterLimit = it }, range = 0..60, step = 5)
    }

    DemoTextField(
        value = fieldValue,
        onValueChange = { fieldValue = it },
        label = fieldLabel,
        placeholder = fieldPlaceholder,
        supportingText = helperText,
        leadingIcon = selectedIcon.vector,
        enabled = isEnabled,
        maxLength = characterLimit.takeIf { it > 0 },
        modifier = Modifier.width(360.dp),
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Text field with validation",
    component = CStoryComponentRefs.DemoTextField,
)
@Composable
fun DemoTextFieldValidationStory() {
    var password by remember { mutableStateOf("abc") }
    var minLength by remember { mutableStateOf(8) }
    val hasError = password.isNotEmpty() && password.length < minLength

    KnobPanel {
        IntKnob(label = "Minimum length", value = minLength, onValueChange = { minLength = it }, range = 4..16)
    }

    DemoTextField(
        value = password,
        onValueChange = { password = it },
        label = "Password",
        supportingText = "At least $minLength characters",
        isError = hasError,
        errorMessage = "Password is too short (${password.length} / $minLength)",
        modifier = Modifier.width(360.dp),
    )
}
