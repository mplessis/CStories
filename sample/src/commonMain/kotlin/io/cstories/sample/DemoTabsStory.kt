package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

private val sampleTabs = listOf(
    TabItem("Overview", SampleIcon.Inbox.vector),
    TabItem("Activity", SampleIcon.Notifications.vector, count = 3),
    TabItem("Members", SampleIcon.Person.vector, count = 12),
    TabItem("Favorites", SampleIcon.Favorite.vector),
    TabItem("Search", SampleIcon.Search.vector),
    TabItem("Settings", SampleIcon.Settings.vector),
)

@CStory(
    collection = "DesignSystem",
    group = "Navigation",
    name = "Tabs",
    component = CStoryComponentRefs.DemoTabs,
)
@Composable
fun DemoTabsStory() {
    var tabCount by remember { mutableStateOf(3) }
    var selectedTab by remember { mutableStateOf(0) }
    var showIcons by remember { mutableStateOf(false) }
    var isScrollable by remember { mutableStateOf(false) }
    val visibleTabs = sampleTabs.take(tabCount).map { if (showIcons) it else it.copy(icon = null) }

    KnobPanel {
        IntKnob(label = "Number of tabs", value = tabCount, onValueChange = { tabCount = it }, range = 2..sampleTabs.size)
        BooleanKnob(label = "Show icons", value = showIcons, onValueChange = { showIcons = it })
        BooleanKnob(label = "Scrollable", value = isScrollable, onValueChange = { isScrollable = it }, codeKey = "isScrollable")
    }

    Column(modifier = Modifier.width(480.dp)) {
        DemoTabs(
            tabs = visibleTabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            scrollable = isScrollable,
        )
        Text(
            text = "Content of ${(visibleTabs.getOrNull(selectedTab) ?: visibleTabs.first()).label}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(24.dp),
        )
    }
}
