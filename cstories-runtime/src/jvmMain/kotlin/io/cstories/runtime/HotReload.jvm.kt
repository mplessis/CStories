package io.cstories.runtime

import org.jetbrains.compose.reload.isHotReloadActive as composeHotReloadIsActive

internal actual val hotReloadCommand: String = "./gradlew runCStoriesDesktopHotReload"

internal actual fun isHotReloadActive(): Boolean = composeHotReloadIsActive
