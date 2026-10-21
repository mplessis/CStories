package io.cstories.runtime

import kotlinx.browser.localStorage

internal actual fun loadSidebarWidth(): Float? =
    localStorage.getItem(SIDEBAR_WIDTH_STORAGE_KEY)?.toFloatOrNull()

internal actual fun saveSidebarWidth(widthDp: Float) {
    localStorage.setItem(SIDEBAR_WIDTH_STORAGE_KEY, widthDp.toString())
}
