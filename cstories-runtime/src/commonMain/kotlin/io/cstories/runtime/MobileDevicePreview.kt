package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dimensions and visual proportions of a simulated mobile device. */
data class MobileDevice(
    val width: Dp = 390.dp,
    val height: Dp = 844.dp,
    val cornerRadius: Dp = 36.dp,
) {
    companion object {
        val Default = MobileDevice()
    }
}

/**
 * Renders content inside a device-shaped viewport for mobile screen previews.
 *
 * The viewport is intentionally fixed-size so that a story can be checked at
 * a predictable mobile resolution. Content can scroll vertically inside the
 * simulated screen when it is taller than the viewport.
 */
@Composable
fun MobileDevicePreview(
    modifier: Modifier = Modifier,
    device: MobileDevice = MobileDevice.Default,
    content: @Composable BoxScope.() -> Unit,
) {
    val screenShape = remember(device.cornerRadius) {
        RoundedCornerShape(device.cornerRadius)
    }
    val screenColor = MaterialThemeColorsForPreview.screen
    val frameColor = MaterialThemeColorsForPreview.frame

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = screenShape,
                ambientColor = Color.Black.copy(alpha = 0.24f),
                spotColor = Color.Black.copy(alpha = 0.32f),
            )
            .clip(screenShape)
            .background(frameColor)
            .border(1.dp, Color.Black.copy(alpha = 0.2f), screenShape)
            .padding(5.dp)
            .background(screenColor, screenShape)
            .size(device.width, device.height),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(screenShape)
                .verticalScroll(rememberScrollState())
                .padding(top = 22.dp, start = 18.dp, end = 18.dp, bottom = 18.dp),
            content = content,
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .padding(horizontal = 34.dp, vertical = 5.dp),
        )
    }
}

private object MaterialThemeColorsForPreview {
    val frame = Color(0xFF202124)
    val screen = Color(0xFFFDFBFF)
}
