package io.cstories.runtime

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

/** Builds a plain-text [ClipEntry], whose construction is platform-specific. */
internal expect fun plainTextClipEntry(text: String): ClipEntry

/** Copies [text] to the system clipboard as plain text. */
internal suspend fun Clipboard.setPlainText(text: String) {
    setClipEntry(plainTextClipEntry(text))
}
