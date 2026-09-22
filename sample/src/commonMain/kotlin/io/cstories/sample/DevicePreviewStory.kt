package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.runtime.DesktopDevice
import io.cstories.runtime.DevicePreview
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
        desktopDevice = DesktopDevice(title = title),
    ) {
        DevicePreviewDemoScreen(title = title, showAction = showAction)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DevicePreviewDemoScreen(title: String, showAction: Boolean) {

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SummaryCard("Stories", "24")
            SummaryCard("Components", "12")
            SummaryCard("Coverage", "86%")
        }
        repeat(5) { index ->
            Text(
                text = "Recent activity ${index + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F3F4), MaterialTheme.shapes.medium)
                    .padding(16.dp),
            )
        }
        if (showAction) {
            Button(onClick = {}) {
                Text("Create story")
            }
        }
    }

}

@Composable
private fun SummaryCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
