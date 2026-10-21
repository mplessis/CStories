package io.cstories.runtime

import java.util.prefs.Preferences

private val preferences: Preferences by lazy { Preferences.userRoot().node("io/cstories/runtime") }

internal actual fun loadSidebarWidth(): Float? =
    runCatching { preferences.get(SIDEBAR_WIDTH_STORAGE_KEY, null)?.toFloatOrNull() }.getOrNull()

internal actual fun saveSidebarWidth(widthDp: Float) {
    runCatching {
        preferences.putFloat(SIDEBAR_WIDTH_STORAGE_KEY, widthDp)
        preferences.flush()
    }
}
