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
    group = "Feedback",
    name = "Progress indicator",
    component = CStoryComponentRefs.DemoProgressIndicator,
)
@Composable
fun DemoProgressIndicatorStory() {
    var percent by remember { mutableStateOf(40) }
    var isIndeterminate by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf(ProgressStyle.Linear) }
    var progressLabel by remember { mutableStateOf("Uploading report.pdf") }
    var showPercent by remember { mutableStateOf(true) }

    KnobPanel {
        IntKnob(label = "Progress (%)", value = percent, onValueChange = { percent = it }, range = 0..100, step = 5, codeKey = "percent")
        BooleanKnob(label = "Indeterminate", value = isIndeterminate, onValueChange = { isIndeterminate = it })
        SelectKnob(label = "Style", value = selectedStyle, onValueChange = { selectedStyle = it }, codeKey = "selectedStyle")
        TextKnob(label = "Label", value = progressLabel, onValueChange = { progressLabel = it }, codeKey = "progressLabel")
        BooleanKnob(label = "Show percentage", value = showPercent, onValueChange = { showPercent = it }, codeKey = "showPercent")
    }

    DemoProgressIndicator(
        progress = if (isIndeterminate) null else percent,
        style = selectedStyle,
        label = progressLabel,
        showPercentage = showPercent,
        modifier = Modifier.width(360.dp),
    )
}
