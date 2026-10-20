package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "List item",
    component = CStoryComponentRefs.DemoListItem,
)
@Composable
fun DemoListItemStory() {
    var itemTitle by remember { mutableStateOf("Ada Lovelace") }
    var itemSubtitle by remember { mutableStateOf("Reviewed your pull request") }
    var showLeading by remember { mutableStateOf(true) }
    var showTrailing by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = itemTitle, onValueChange = { itemTitle = it }, codeKey = "itemTitle")
        TextKnob(label = "Subtitle", value = itemSubtitle, onValueChange = { itemSubtitle = it }, codeKey = "itemSubtitle")
        BooleanKnob(label = "Leading avatar", value = showLeading, onValueChange = { showLeading = it })
        BooleanKnob(label = "Trailing badge", value = showTrailing, onValueChange = { showTrailing = it })
    }

    DemoListItem(
        title = itemTitle,
        subtitle = itemSubtitle,
        onClick = {},
        modifier = Modifier.width(400.dp),
        leading = if (showLeading) ({ DemoAvatar(name = itemTitle, presence = Presence.Online) }) else null,
        trailing = if (showTrailing) ({ DemoBadge(text = "New", tone = StatusTone.Info, size = BadgeSize.Small) }) else null,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "List",
    component = CStoryComponentRefs.DemoListItem,
)
@Composable
fun DemoListItemListStory() {
    var itemCount by remember { mutableStateOf(4) }

    KnobPanel {
        IntKnob(label = "Number of items", value = itemCount, onValueChange = { itemCount = it }, range = 1..sampleMembers.size)
    }

    Column(modifier = Modifier.width(400.dp)) {
        sampleMembers.take(itemCount).forEachIndexed { index, (memberName, memberRole) ->
            if (index > 0) HorizontalDivider()
            DemoListItem(
                title = memberName,
                subtitle = memberRole,
                onClick = {},
                leading = { DemoAvatar(name = memberName) },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

internal val sampleMembers = listOf(
    "Ada Lovelace" to "Owner",
    "Alan Turing" to "Admin",
    "Grace Hopper" to "Editor",
    "Linus Torvalds" to "Editor",
    "Margaret Hamilton" to "Viewer",
    "Dennis Ritchie" to "Viewer",
    "Barbara Liskov" to "Viewer",
    "Ken Thompson" to "Guest",
)
