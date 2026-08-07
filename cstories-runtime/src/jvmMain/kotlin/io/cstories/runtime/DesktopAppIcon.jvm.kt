package io.cstories.runtime

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.logo
import java.awt.Taskbar
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.jetbrains.compose.resources.painterResource

private const val DESKTOP_ICON_PATH = "io/cstories/runtime/logo-desktop.png"

@Composable
fun cstoriesDesktopWindowIcon(): Painter = painterResource(Res.drawable.logo)

fun configureDesktopAppIcon() {
    if (!Taskbar.isTaskbarSupported()) return

    runCatching {
        val image = loadDesktopAppIcon() ?: return
        Taskbar.getTaskbar().iconImage = image
    }
}

private fun loadDesktopAppIcon(): BufferedImage? {
    val resourceStream = Thread.currentThread().contextClassLoader
        ?.getResourceAsStream(DESKTOP_ICON_PATH)
        ?: DesktopAppIcon::class.java.classLoader?.getResourceAsStream(DESKTOP_ICON_PATH)
        ?: return null

    resourceStream.use { return ImageIO.read(it) }
}

private object DesktopAppIcon
