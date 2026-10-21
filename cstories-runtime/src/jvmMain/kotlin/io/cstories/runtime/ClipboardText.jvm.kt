package io.cstories.runtime

import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

internal actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry(StringSelection(text))
