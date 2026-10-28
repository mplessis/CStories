package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Renders content inside a device-shaped viewport for mobile screen previews.
 *
 * By default, the content receives the finite height of the simulated screen, like on a real device, so
 * full-height layouts (`Scaffold`, `fillMaxSize`, `LazyColumn`, `Modifier.weight`…) render correctly.
 *
 * @param scrollable when `true`, the content scrolls vertically when it is taller than the simulated screen.
 * It is then measured with an unbounded height and must not try to fill the available height.
 * @param contentPadding padding applied around the content inside the simulated screen. Use `PaddingValues(0.dp)`
 * for full-screen content such as a `Scaffold`.
 */
@Composable
fun MobileDevicePreview(
    modifier: Modifier = Modifier,
    device: MobileDevice = MobileDevice.Default,
    additionalMobileDevices: List<MobileDevice> = emptyList(),
    registerInToolbar: Boolean = true,
    scrollable: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(top = 22.dp, start = 18.dp, end = 18.dp, bottom = 18.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val previewSlot = LocalDevicePreviewSlot.current
    var selectedDevice by remember(device.id) { mutableStateOf(device) }
    val mobileDevices = rememberMobileDevices(additionalMobileDevices)

    if (previewSlot != null && registerInToolbar) {
        SideEffect {
            previewSlot.value = DevicePreviewRegistration(
                selectedMobileDevice = selectedDevice,
                mobileDevices = mobileDevices,
                onMobileDeviceSelected = { selectedDevice = it },
                supportsDeviceTypeSelection = false,
            )
        }
    }

    val deviceShape = remember(selectedDevice.cornerRadius) {
        RoundedCornerShape(selectedDevice.cornerRadius)
    }
    val screenShape = remember(selectedDevice.cornerRadius) {
        RoundedCornerShape((selectedDevice.cornerRadius - 5.dp).coerceAtLeast(0.dp))
    }

    Box(
        modifier = modifier
            .shadow(18.dp, deviceShape, ambientColor = Color.Black.copy(alpha = 0.24f), spotColor = Color.Black.copy(alpha = 0.32f))
            .clip(deviceShape)
            .background(Color(0xFF202124))
            .border(1.dp, Color.Black.copy(alpha = 0.2f), deviceShape)
            .padding(5.dp)
            .size(selectedDevice.width, selectedDevice.height),
        contentAlignment = Alignment.TopCenter,
    ) {
        PreviewThemedContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(screenShape)
                    .background(MaterialTheme.colorScheme.background)
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(contentPadding),
                content = content,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .padding(horizontal = 34.dp, vertical = 5.dp),
        )
    }
}
