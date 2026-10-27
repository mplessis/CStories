package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * Description of one tab of a [DemoTabs] row.
 *
 * @property label Text of the tab.
 * @property icon Optional icon displayed above the label.
 * @property count Optional counter displayed next to the label, e.g. unread items. Hidden when `null` or `0`.
 */
@Immutable
data class TabItem(
    val label: String,
    val icon: ImageVector? = null,
    val count: Int? = null,
)

/**
 * A horizontal row of tabs to switch between sibling sections of the
 * same screen, with an indicator under the selected tab.
 *
 * ### Usage
 * - Use tabs for 2 to 6 sections at the same level ("Overview / Activity / Settings").
 *   For filters of the same content, use a `DemoSegmentedControl`.
 * - Labels should be one or two words and must not wrap.
 * - Set [scrollable] to `true` when the tabs don't fit the width; tabs then keep their natural
 *   width and the row scrolls horizontally.
 * - Use [TabItem.count] for a number that needs attention, such as unread messages.
 * - The component only renders the tab row: display the content of the selected section below it.
 *
 * ### Accessibility
 * - Each tab is announced as a tab with its label and selected state.
 * - Counters are part of the tab label, so they are announced too.
 *
 * @param tabs The tabs to display, in order. See [TabItem].
 * @param selectedIndex Index of the selected tab in [tabs].
 * @param onTabSelected Called with the index of the clicked tab.
 * @param modifier Modifier applied to the tab row.
 * @param scrollable When `true`, tabs are not stretched to fill the width and the row scrolls horizontally.
 * @see TabItem
 */
@CStoryComponent
@Composable
fun DemoTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    val safeIndex = selectedIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    val tabsContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == safeIndex,
                onClick = { onTabSelected(index) },
                icon = tab.icon?.let { { Icon(it, contentDescription = null) } },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(tab.label, maxLines = 1)
                        if (tab.count != null && tab.count > 0) {
                            Badge { Text(if (tab.count > 99) "99+" else tab.count.toString()) }
                        }
                    }
                },
            )
        }
    }

    if (scrollable) {
        PrimaryScrollableTabRow(selectedTabIndex = safeIndex, modifier = modifier, edgePadding = 16.dp) { tabsContent() }
    } else {
        PrimaryTabRow(selectedTabIndex = safeIndex, modifier = modifier) { tabsContent() }
    }
}
