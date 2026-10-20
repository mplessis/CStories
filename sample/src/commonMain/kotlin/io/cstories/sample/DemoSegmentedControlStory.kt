package io.cstories.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.IntKnob
import io.cstories.runtime.knobs.KnobPanel

/** Time ranges offered by the segmented control story. */
enum class TimeRange { Day, Week, Month, Quarter, Year }

@CStory(
    collection = "DesignSystem",
    group = "Inputs",
    name = "Segmented control",
    component = CStoryComponentRefs.DemoSegmentedControl,
)
@Composable
fun DemoSegmentedControlStory() {
    var segmentCount by remember { mutableStateOf(3) }
    var selectedRange by remember { mutableStateOf(TimeRange.Week) }
    var isEnabled by remember { mutableStateOf(true) }
    val visibleRanges = TimeRange.entries.take(segmentCount)
    val currentRange = if (selectedRange in visibleRanges) selectedRange else visibleRanges.first()

    KnobPanel {
        IntKnob(label = "Number of segments", value = segmentCount, onValueChange = { segmentCount = it }, range = 2..TimeRange.entries.size)
        BooleanKnob(label = "Enabled", value = isEnabled, onValueChange = { isEnabled = it }, codeKey = "isEnabled")
    }

    DemoSegmentedControl(
        options = visibleRanges,
        selected = currentRange,
        onSelectedChange = { selectedRange = it },
        labelOf = { it.name },
        enabled = isEnabled,
    )
}
