package io.cstories.runtime

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

internal data class DevicePreviewRegistration(
    val selectedDevice: PreviewDevice = PreviewDevice.Mobile,
    val onDeviceSelected: (PreviewDevice) -> Unit = {},
    val selectedMobileDevice: MobileDevice = MobileDevice.Default,
    val mobileDevices: List<MobileDevice> = emptyList(),
    val onMobileDeviceSelected: (MobileDevice) -> Unit = {},
    val supportsDeviceTypeSelection: Boolean = true,
)

internal val LocalDevicePreviewSlot: ProvidableCompositionLocal<MutableState<DevicePreviewRegistration?>?> =
    staticCompositionLocalOf { null }
