package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Switch row",
    component = CStoryComponentRefs.DemoSwitchRow,
)
@Composable
fun DemoSwitchRowStory() {
    var settingTitle by remember { mutableStateOf("Push notifications") }
    var settingDescription by remember { mutableStateOf("Get notified when someone mentions you.") }
    var isChecked by remember { mutableStateOf(true) }
    var isEnabled by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = settingTitle, onValueChange = { settingTitle = it }, codeKey = "settingTitle")
        TextKnob(label = "Description", value = settingDescription, onValueChange = { settingDescription = it }, codeKey = "settingDescription")
        BooleanKnob(label = "Checked", value = isChecked, onValueChange = { isChecked = it }, codeKey = "isChecked")
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoSwitchRow(
        title = settingTitle,
        description = settingDescription,
        checked = isChecked,
        onCheckedChange = { isChecked = it },
        enabled = isEnabled,
        modifier = Modifier.width(400.dp),
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Settings list",
    component = CStoryComponentRefs.DemoSwitchRow,
)
@Composable
fun DemoSwitchRowSettingsStory() {
    var darkMode by remember { mutableStateOf(false) }
    var autoSave by remember { mutableStateOf(true) }
    var analytics by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(400.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        DemoSwitchRow(title = "Dark mode", checked = darkMode, onCheckedChange = { darkMode = it })
        DemoSwitchRow(
            title = "Auto-save",
            description = "Save your drafts every 30 seconds.",
            checked = autoSave,
            onCheckedChange = { autoSave = it },
        )
        DemoSwitchRow(
            title = "Share usage analytics",
            description = "Managed by your organization.",
            checked = analytics,
            onCheckedChange = { analytics = it },
            enabled = false,
        )
    }
}
