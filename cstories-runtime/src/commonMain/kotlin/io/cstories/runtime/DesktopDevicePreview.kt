package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dimensions and visual proportions of a simulated desktop window. */
data class DesktopDevice(
    val width: Dp = 1280.dp,
    val height: Dp = 800.dp,
    val cornerRadius: Dp = 12.dp,
    val title: String = "Desktop preview",
) {
    companion object {
        val Default = DesktopDevice()
    }
}

/**
 * Renders content inside a desktop-window-shaped viewport.
 *
 * The window chrome is decorative. The content viewport has a fixed size so
 * that stories can be checked at a predictable desktop resolution and scrolls
 * vertically when its content is taller than the simulated screen.
 */
@Composable
fun DesktopDevicePreview(
    modifier: Modifier = Modifier,
    device: DesktopDevice = DesktopDevice.Default,
    content: @Composable BoxScope.() -> Unit,
) {
    val windowShape = remember(device.cornerRadius) {
        RoundedCornerShape(device.cornerRadius)
    }

    Column(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = windowShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.28f),
            )
            .clip(windowShape)
            .background(DesktopPreviewColors.frame)
            .border(1.dp, DesktopPreviewColors.frameBorder, windowShape)
            .width(device.width),
    ) {
        DesktopWindowTitleBar(title = device.title)
        Box(
            modifier = Modifier
                .size(width = device.width, height = device.height)
                .background(DesktopPreviewColors.screen)
                .verticalScroll(rememberScrollState()),
            content = content,
        )
    }
}

@Composable
private fun DesktopWindowTitleBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesktopPreviewColors.titleBar)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WindowControl(color = DesktopPreviewColors.close)
        WindowControl(color = DesktopPreviewColors.minimize)
        WindowControl(color = DesktopPreviewColors.maximize)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = DesktopPreviewColors.title,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.width(52.dp))
    }
}

@Composable
private fun WindowControl(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private object DesktopPreviewColors {
    val frame = Color(0xFF202124)
    val frameBorder = Color(0xFF3C4043)
    val titleBar = Color(0xFFF1F3F4)
    val title = Color(0xFF5F6368)
    val screen = Color(0xFFFDFBFF)
    val close = Color(0xFFFF5F57)
    val minimize = Color(0xFFFFBD2E)
    val maximize = Color(0xFF28C840)
}
