package io.cstories.runtime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.device_preview_desktop
import io.cstories.runtime.resources.device_preview_mobile
import io.cstories.runtime.resources.device_preview_mobile_model
import org.jetbrains.compose.resources.stringResource

/** The viewport chrome used by [DevicePreview]. */
enum class PreviewDevice {
    Mobile,
    Desktop,
}

/**
 * Displays the same content in either a simulated mobile device or desktop
 * window, with an in-preview segmented switch to change between them.
 */
@Composable
fun DevicePreview(
    modifier: Modifier = Modifier,
    initialDevice: PreviewDevice = PreviewDevice.Mobile,
    mobileDevice: MobileDevice = MobileDevice.Default,
    desktopDevice: DesktopDevice = DesktopDevice.Default,
    additionalMobileDevices: List<MobileDevice> = emptyList(),
    onDeviceChanged: (PreviewDevice) -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    var selectedDevice by remember { mutableStateOf(initialDevice) }
    var selectedMobileDevice by remember(mobileDevice.id) { mutableStateOf(mobileDevice) }
    val mobileDevices = rememberMobileDevices(additionalMobileDevices)
    val devicePreviewSlot = LocalDevicePreviewSlot.current

    LaunchedEffect(selectedDevice) {
        onDeviceChanged(selectedDevice)
    }

    fun selectDevice(device: PreviewDevice) {
        if (device != selectedDevice) {
            selectedDevice = device
        }
    }

    if (devicePreviewSlot != null) {
        SideEffect {
            devicePreviewSlot.value = DevicePreviewRegistration(
                selectedDevice = selectedDevice,
                onDeviceSelected = ::selectDevice,
                selectedMobileDevice = selectedMobileDevice,
                mobileDevices = mobileDevices,
                onMobileDeviceSelected = { selectedMobileDevice = it },
                supportsDeviceTypeSelection = true,
            )
        }
        DevicePreviewContent(
            selectedDevice = selectedDevice,
            desktopDevice = desktopDevice,
            selectedMobileDevice = selectedMobileDevice,
            modifier = modifier,
            content = content,
        )
    } else {
        androidx.compose.foundation.layout.Column(modifier = modifier) {
            DevicePreviewSelector(
                selectedDevice = selectedDevice,
                onDeviceSelected = ::selectDevice,
                selectedMobileDevice = selectedMobileDevice,
                mobileDevices = mobileDevices,
                onMobileDeviceSelected = { selectedMobileDevice = it },
            )
            DevicePreviewContent(
                selectedDevice = selectedDevice,
                desktopDevice = desktopDevice,
                selectedMobileDevice = selectedMobileDevice,
                content = content,
            )
        }
    }
}

@Composable
private fun DevicePreviewContent(
    selectedDevice: PreviewDevice,
    desktopDevice: DesktopDevice,
    selectedMobileDevice: MobileDevice,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    when (selectedDevice) {
        PreviewDevice.Mobile -> MobileDevicePreview(
            modifier = modifier,
            device = selectedMobileDevice,
            registerInToolbar = false,
            content = content
        )

        PreviewDevice.Desktop -> DesktopDevicePreview(modifier = modifier, device = desktopDevice, content = content)
    }
}

@Composable
internal fun DevicePreviewSelector(
    selectedDevice: PreviewDevice,
    onDeviceSelected: (PreviewDevice) -> Unit,
    selectedMobileDevice: MobileDevice,
    mobileDevices: List<MobileDevice>,
    onMobileDeviceSelected: (MobileDevice) -> Unit,
    showDeviceTypeSelector: Boolean = true,
) {
    val mobileLabel = stringResource(Res.string.device_preview_mobile)
    val desktopLabel = stringResource(Res.string.device_preview_desktop)

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showDeviceTypeSelector) {
            DevicePreviewTypeSelector(selectedDevice, onDeviceSelected, mobileLabel, desktopLabel)
        }
        if (selectedDevice == PreviewDevice.Mobile && mobileDevices.isNotEmpty()) {
            Spacer(Modifier.width(24.dp))
            MobileDeviceSelector(selectedMobileDevice, mobileDevices, onMobileDeviceSelected)
        }
    }
}

@Composable
private fun DevicePreviewTypeSelector(
    selectedDevice: PreviewDevice,
    onDeviceSelected: (PreviewDevice) -> Unit,
    mobileLabel: String,
    desktopLabel: String,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .background(CStoriesColors.surfaceSunken)
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.sm))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewDeviceOption(
            device = PreviewDevice.Mobile,
            label = mobileLabel,
            selected = selectedDevice == PreviewDevice.Mobile,
            contentDescription = mobileLabel,
            onClick = { onDeviceSelected(PreviewDevice.Mobile) },
        )
        PreviewDeviceOption(
            device = PreviewDevice.Desktop,
            label = desktopLabel,
            selected = selectedDevice == PreviewDevice.Desktop,
            contentDescription = desktopLabel,
            onClick = { onDeviceSelected(PreviewDevice.Desktop) },
        )
    }
}

@Composable
private fun MobileDeviceSelector(
    selectedDevice: MobileDevice,
    devices: List<MobileDevice>,
    onDeviceSelected: (MobileDevice) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val mobileDeviceLabel = stringResource(Res.string.device_preview_mobile_model)
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = mobileDeviceLabel,
                color = CStoriesColors.textFaint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(CStoriesRadii.sm))
                    .background(CStoriesColors.surface)
                    .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.sm))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = true },
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = selectedDevice.name,
                        color = CStoriesColors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = CStoriesColors.textFaint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(device.name) },
                    onClick = {
                        onDeviceSelected(device)
                        expanded = false
                    },
                )
            }
        }
    }
}


@Composable
private fun PreviewDeviceOption(
    device: PreviewDevice,
    label: String,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CStoriesRadii.sm - 2.dp))
            .background(
                color = if (selected) CStoriesColors.primary else Color.Transparent,
                shape = RoundedCornerShape(CStoriesRadii.sm - 2.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(5.dp),
        ) {
            PreviewDeviceIcon(
                device = device,
                tint = if (selected) Color.White else CStoriesColors.textFaint,
            )
            Text(
                text = label,
                color = if (selected) Color.White else CStoriesColors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PreviewDeviceIcon(
    device: PreviewDevice,
    tint: Color,
) {
    Canvas(Modifier.size(14.dp)) {
        val width = size.width
        val height = size.height
        if (device == PreviewDevice.Mobile) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(width * 0.28f, height * 0.08f),
                size = Size(width * 0.44f, height * 0.84f),
                cornerRadius = CornerRadius(width * 0.12f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f),
            )
            drawCircle(tint, radius = 0.7f, center = Offset(width / 2f, height * 0.76f))
        } else {
            drawRoundRect(
                color = tint,
                topLeft = Offset(width * 0.08f, height * 0.16f),
                size = Size(width * 0.84f, height * 0.54f),
                cornerRadius = CornerRadius(width * 0.1f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f),
            )
            drawLine(
                color = tint,
                start = Offset(width * 0.5f, height * 0.7f),
                end = Offset(width * 0.5f, height * 0.86f),
                strokeWidth = 1.4f,
            )
            drawLine(
                color = tint,
                start = Offset(width * 0.28f, height * 0.86f),
                end = Offset(width * 0.72f, height * 0.86f),
                strokeWidth = 1.4f,
            )
        }
    }
}
