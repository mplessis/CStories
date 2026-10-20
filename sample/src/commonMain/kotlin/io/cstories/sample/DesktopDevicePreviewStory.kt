package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import io.cstories.runtime.DesktopDevice
import io.cstories.runtime.DesktopDevicePreview
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(collection = "Runtime", group = "Previews", name = "Desktop window")
@Composable
fun DesktopDevicePreviewStory() {
    var title by remember { mutableStateOf("Workspace") }
    var showAction by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = title, onValueChange = { title = it })
        BooleanKnob(label = "Show action", value = showAction, onValueChange = { showAction = it })
    }

    DesktopDevicePreview(device = DesktopDevice(title = title)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 24.dp),
            ) {
                Text(
                    text = "CStories",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                listOf(Icons.Filled.Inbox to "Overview", Icons.Filled.Person to "Members", Icons.Filled.Settings to "Settings")
                    .forEach { (icon, label) ->
                        DemoListItem(
                            title = label,
                            onClick = {},
                            leading = { Icon(icon, contentDescription = null) },
                        )
                    }
            }

            Column(modifier = Modifier.weight(1f)) {
                DemoTopBar(title = title, subtitle = "Welcome back")
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DemoStatCard(label = "Stories", value = "24", trendPercent = 9, modifier = Modifier.width(180.dp))
                        DemoStatCard(label = "Components", value = "12", trendPercent = 0, modifier = Modifier.width(180.dp))
                        DemoStatCard(label = "Coverage", value = "86%", trendPercent = 4, modifier = Modifier.width(180.dp))
                    }
                    Column {
                        sampleMembers.forEach { (memberName, memberRole) ->
                            DemoListItem(
                                title = memberName,
                                subtitle = "Updated a story",
                                leading = { DemoAvatar(name = memberName, size = AvatarSize.Small) },
                                trailing = { DemoBadge(text = memberRole, size = BadgeSize.Small) },
                            )
                        }
                    }
                    if (showAction) {
                        DemoButton(text = "Create story", onClick = {}, leadingIcon = SampleIcon.Add.vector)
                    }
                }
            }
        }
    }
}
