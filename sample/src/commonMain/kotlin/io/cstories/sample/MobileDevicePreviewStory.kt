package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.runtime.MobileDevicePreview
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(collection = "Runtime", group = "Previews", name = "Mobile device")
@Composable
fun MobileDevicePreviewStory() {
    var title by remember { mutableStateOf("My activity") }
    var showAction by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = title, onValueChange = { title = it })
        BooleanKnob(label = "Show action", value = showAction, onValueChange = { showAction = it })
    }

    MobileDevicePreview(scrollable = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DemoTopBar(title = title, onBack = {})

            DemoProgressIndicator(
                progress = 70,
                label = "Weekly progress",
                showPercentage = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            Column {
                sampleMembers.forEachIndexed { index, (memberName, _) ->
                    DemoListItem(
                        title = "Activity ${index + 1}",
                        subtitle = "Completed today by $memberName",
                        leading = { DemoAvatar(name = memberName, size = AvatarSize.Small) },
                        trailing = if (index == 0) ({ DemoBadge(text = "New", tone = StatusTone.Info, size = BadgeSize.Small) }) else null,
                    )
                }
            }

            if (showAction) {
                DemoButton(
                    text = "Continue",
                    onClick = {},
                    size = ButtonSize.Large,
                    fullWidth = true,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
