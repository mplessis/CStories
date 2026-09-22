package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
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

    DesktopDevicePreview(device = io.cstories.runtime.DesktopDevice(title = title)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(210.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF202A44))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("CStories", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("Overview", color = Color(0xFFD0BCFF))
                Text("Projects", color = Color(0xFFB8C0D9))
                Text("Settings", color = Color(0xFFB8C0D9))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Welcome back", style = MaterialTheme.typography.labelLarge)
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard("Stories", "24")
                    SummaryCard("Components", "12")
                    SummaryCard("Coverage", "86%")
                }
                repeat(8) { index ->
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
