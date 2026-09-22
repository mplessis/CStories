package io.cstories.runtime

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

internal data class DevicePreviewRegistration(
    val selectedDevice: PreviewDevice,
    val onDeviceSelected: (PreviewDevice) -> Unit,
)

internal val LocalDevicePreviewSlot: ProvidableCompositionLocal<MutableState<DevicePreviewRegistration?>?> =
    staticCompositionLocalOf { null }
