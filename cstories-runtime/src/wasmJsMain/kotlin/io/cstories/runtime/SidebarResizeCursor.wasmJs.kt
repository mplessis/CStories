package io.cstories.runtime

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.fromKeyword
import androidx.compose.ui.input.pointer.pointerHoverIcon

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun Modifier.sidebarResizeCursor(): Modifier {
    return pointerHoverIcon(PointerIcon.fromKeyword("ew-resize"))
}
