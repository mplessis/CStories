package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "Stat card",
    component = CStoryComponentRefs.DemoStatCard,
)
@Composable
fun DemoStatCardStory() {
    var metricLabel by remember { mutableStateOf("Active users") }
    var metricValue by remember { mutableStateOf("1,284") }
    var showTrend by remember { mutableStateOf(true) }
    var trend by remember { mutableStateOf(12) }
    var isHigherBetter by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Label", value = metricLabel, onValueChange = { metricLabel = it }, codeKey = "metricLabel")
        TextKnob(label = "Value", value = metricValue, onValueChange = { metricValue = it }, codeKey = "metricValue")
        BooleanKnob(label = "Show trend", value = showTrend, onValueChange = { showTrend = it })
        IntKnob(label = "Trend (%)", value = trend, onValueChange = { trend = it }, range = -50..50, codeKey = "trend")
        BooleanKnob(label = "Higher is better", value = isHigherBetter, onValueChange = { isHigherBetter = it }, codeKey = "isHigherBetter")
    }

    DemoStatCard(
        label = metricLabel,
        value = metricValue,
        trendPercent = if (showTrend) trend else null,
        higherIsBetter = isHigherBetter,
        modifier = Modifier.width(220.dp),
    )
}

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "Stat cards dashboard",
    component = CStoryComponentRefs.DemoStatCard,
)
@Composable
fun DemoStatCardDashboardStory() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoStatCard(label = "Stories", value = "24", trendPercent = 9, modifier = Modifier.width(180.dp))
        DemoStatCard(label = "Coverage", value = "86%", trendPercent = 0, modifier = Modifier.width(180.dp))
        DemoStatCard(
            label = "Failed builds",
            value = "3",
            trendPercent = -40,
            higherIsBetter = false,
            modifier = Modifier.width(180.dp),
        )
    }
}
