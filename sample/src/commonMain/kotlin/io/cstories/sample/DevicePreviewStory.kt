package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.runtime.DesktopDevice
import io.cstories.runtime.DevicePreview
import io.cstories.runtime.MobileDevice
import io.cstories.runtime.PreviewDevice
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@OptIn(ExperimentalLayoutApi::class)
@CStory(collection = "Runtime", group = "Previews", name = "Device preview")
@Composable
fun DevicePreviewStory() {
    var title by remember { mutableStateOf("Workspace") }
    var showAction by remember { mutableStateOf(true) }

    KnobPanel {
        TextKnob(label = "Title", value = title, onValueChange = { title = it })
        BooleanKnob(label = "Show action", value = showAction, onValueChange = { showAction = it })
    }

    DevicePreview(
        initialDevice = PreviewDevice.Desktop,
        mobileDevice = MobileDevice(
            id = "cstories-custom-phone",
            name = "CStories Phone",
            width = 393.dp,
            height = 852.dp,
        ),
        desktopDevice = DesktopDevice(title = title),
        additionalMobileDevices = listOf(
            MobileDevice(
                id = "cstories-custom-phone",
                name = "CStories Phone",
                width = 393.dp,
                height = 852.dp,
            ),
        ),
        scrollable = true,
    ) {
        DevicePreviewDemoScreen(title = title, showAction = showAction)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DevicePreviewDemoScreen(title: String, showAction: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoTopBar(title = title, subtitle = "CStories")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DemoStatCard(label = "Stories", value = "24", trendPercent = 9, modifier = Modifier.width(160.dp))
                DemoStatCard(label = "Components", value = "12", modifier = Modifier.width(160.dp))
                DemoStatCard(label = "Coverage", value = "86%", trendPercent = 4, modifier = Modifier.width(160.dp))
            }
            sampleMembers.take(5).forEach { (memberName, memberRole) ->
                DemoListItem(
                    title = memberName,
                    subtitle = "Updated a story · $memberRole",
                    leading = { DemoAvatar(name = memberName, size = AvatarSize.Small) },
                )
            }
            if (showAction) {
                DemoButton(text = "Create story", onClick = {}, leadingIcon = SampleIcon.Add.vector)
            }
        }
    }
}
