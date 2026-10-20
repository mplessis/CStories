package io.cstories.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Primary",
    component = CStoryComponentRefs.DemoButton,
)
@Composable
fun DemoButtonPrimaryStory() {
    var label by remember { mutableStateOf("Save changes") }
    var isEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
        BooleanKnob(label = "Loading", value = isLoading, onValueChange = { isLoading = it }, codeKey = "isLoading")
    }

    DemoButton(
        text = label,
        onClick = {},
        variant = ButtonVariant.Primary,
        enabled = isEnabled,
        loading = isLoading,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Secondary",
    component = CStoryComponentRefs.DemoButton,
)
@Composable
fun DemoButtonSecondaryStory() {
    var label by remember { mutableStateOf("Cancel") }
    var isEnabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoButton(
        text = label,
        onClick = {},
        variant = ButtonVariant.Secondary,
        enabled = isEnabled,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Tertiary",
    component = CStoryComponentRefs.DemoButton,
)
@Composable
fun DemoButtonTertiaryStory() {
    var label by remember { mutableStateOf("Learn more") }
    var isEnabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoButton(
        text = label,
        onClick = {},
        variant = ButtonVariant.Tertiary,
        enabled = isEnabled,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Danger",
    component = CStoryComponentRefs.DemoButton,
)
@Composable
fun DemoButtonDangerStory() {
    var label by remember { mutableStateOf("Delete project") }
    var selectedIcon by remember { mutableStateOf(SampleIcon.Delete) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        SelectKnob(label = "Leading icon", value = selectedIcon, onValueChange = { selectedIcon = it }, codeKey = "selectedIcon")
    }

    DemoButton(
        text = label,
        onClick = {},
        variant = ButtonVariant.Danger,
        leadingIcon = selectedIcon.vector,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Playground",
    component = CStoryComponentRefs.DemoButton,
)
@Composable
fun DemoButtonPlaygroundStory() {
    var label by remember { mutableStateOf("Send invitation") }
    var selectedVariant by remember { mutableStateOf(ButtonVariant.Primary) }
    var selectedSize by remember { mutableStateOf(ButtonSize.Medium) }
    var selectedIcon by remember { mutableStateOf(SampleIcon.Send) }
    var isEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var isFullWidth by remember { mutableStateOf(false) }

    KnobPanel {
        TextKnob(label = "Text", value = label, onValueChange = { label = it }, codeKey = "label")
        SelectKnob(label = "Variant", value = selectedVariant, onValueChange = { selectedVariant = it }, codeKey = "selectedVariant")
        SelectKnob(label = "Size", value = selectedSize, onValueChange = { selectedSize = it }, codeKey = "selectedSize")
        SelectKnob(label = "Leading icon", value = selectedIcon, onValueChange = { selectedIcon = it }, codeKey = "selectedIcon")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
        BooleanKnob(label = "Loading", value = isLoading, onValueChange = { isLoading = it }, codeKey = "isLoading")
        BooleanKnob(label = "Full width", value = isFullWidth, onValueChange = { isFullWidth = it }, codeKey = "isFullWidth")
    }

    DemoButton(
        text = label,
        onClick = {},
        variant = selectedVariant,
        size = selectedSize,
        enabled = isEnabled,
        loading = isLoading,
        leadingIcon = selectedIcon.vector,
        fullWidth = isFullWidth,
    )
}
