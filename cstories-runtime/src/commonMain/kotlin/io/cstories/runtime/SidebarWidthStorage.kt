package io.cstories.runtime

/** Returns the sidebar width (in dp) saved by [saveSidebarWidth], or `null` if none was saved yet. */
internal expect fun loadSidebarWidth(): Float?

/** Persists the sidebar width (in dp) so the catalog reopens with the same width. */
internal expect fun saveSidebarWidth(widthDp: Float)

internal const val SIDEBAR_WIDTH_STORAGE_KEY = "cstories.sidebarWidth"
