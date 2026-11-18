package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.runtime.DesktopDevicePreview
import io.cstories.runtime.MobileDevicePreview
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(collection = "Foundations", group = "Typography", name = "All styles")
@Composable
fun TypographyAllStylesStory() {
    val sample = rememberTypographySample()
    TypographyStyles(sample)
}

@CStory(collection = "Foundations", group = "Typography", name = "All styles (mobile)")
@Composable
fun TypographyAllStylesMobileStory() {
    val sample = rememberTypographySample()
    MobileDevicePreview {
        TypographyStyles(sample)
    }
}

@CStory(collection = "Foundations", group = "Typography", name = "All styles (desktop)")
@Composable
fun TypographyAllStylesDesktopStory() {
    val sample = rememberTypographySample()
    DesktopDevicePreview {
        TypographyStyles(sample, modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun rememberTypographySample(): String {
    var sample by remember { mutableStateOf("The quick brown fox") }
    KnobPanel {
        TextKnob(label = "Sample text", value = sample, onValueChange = { sample = it })
    }
    return sample
}

@Composable
private fun TypographyStyles(sample: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        typographyStyles(MaterialTheme.typography).forEach { (name, style) ->
            Column {
                Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(text = sample, style = style, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

private fun typographyStyles(typography: Typography): List<Pair<String, TextStyle>> = listOf(
    "displayLarge" to typography.displayLarge,
    "displayMedium" to typography.displayMedium,
    "displaySmall" to typography.displaySmall,
    "headlineLarge" to typography.headlineLarge,
    "headlineMedium" to typography.headlineMedium,
    "headlineSmall" to typography.headlineSmall,
    "titleLarge" to typography.titleLarge,
    "titleMedium" to typography.titleMedium,
    "titleSmall" to typography.titleSmall,
    "bodyLarge" to typography.bodyLarge,
    "bodyMedium" to typography.bodyMedium,
    "bodySmall" to typography.bodySmall,
    "labelLarge" to typography.labelLarge,
    "labelMedium" to typography.labelMedium,
    "labelSmall" to typography.labelSmall,
)
