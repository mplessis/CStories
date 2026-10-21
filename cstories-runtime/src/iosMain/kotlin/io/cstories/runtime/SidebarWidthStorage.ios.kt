package io.cstories.runtime

import platform.Foundation.NSUserDefaults

internal actual fun loadSidebarWidth(): Float? {
    val defaults = NSUserDefaults.standardUserDefaults
    if (defaults.objectForKey(SIDEBAR_WIDTH_STORAGE_KEY) == null) return null
    return defaults.floatForKey(SIDEBAR_WIDTH_STORAGE_KEY)
}

internal actual fun saveSidebarWidth(widthDp: Float) {
    NSUserDefaults.standardUserDefaults.setFloat(widthDp, SIDEBAR_WIDTH_STORAGE_KEY)
}
