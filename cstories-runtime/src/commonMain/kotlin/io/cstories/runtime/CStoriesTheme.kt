package io.cstories.runtime

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Color tokens and shape constants mirroring the "Vela-inspired light fintech"
 * palette from the runtime design mockup (design/version1 - ok.html).
 */
object CStoriesColors {
    val pageBg = Color(0xFFECEEF2)
    val surface = Color(0xFFFFFFFF)
    val surfaceMuted = Color(0xFFF6F7F9)
    val surfaceSunken = Color(0xFFF0F1F4)
    val border = Color(0xFFE7E9EE)
    val borderSoft = Color(0xFFEFF1F4)
    val text = Color(0xFF14161A)
    val textMuted = Color(0xFF6B7280)
    val textFaint = Color(0xFF9AA1AC)
    val primary = Color(0xFFC53364)
    val primarySoft = Color(0xFFE8F0FE)
    val dark = Color(0xFF4d4d4d)
    val success = Color(0xFF16A34A)
    val successBg = Color(0xFFDCFCE7)
    val warning = Color(0xFFB45309)
    val warningBg = Color(0xFFFEF3C7)
    val error = Color(0xFFDC2626)
    val errorBg = Color(0xFFFEE2E2)

    /**
     * Icon tint cycled by nesting depth for the sidebar's nested group
     * headers (see `GroupHeaderRow` in Sidebar.kt), so groups within groups
     * remain visually distinguishable. Index 0 is the first nested level;
     * depths beyond the list size wrap around via modulo.
     */
    val groupLevelColors = listOf(
        primary,
        warning,
        Color(0xFF0891B2),
        Color(0xFF7C3AED),
    )

    // Kotlin syntax highlighting (see KotlinSyntaxHighlighter / CodeBlock).
    val codeKeyword = Color(0xFF9B2393)
    val codeString = Color(0xFF1A7F37)
    val codeStringInterpolation = Color(0xFF0550AE)
    val codeNumber = Color(0xFF0550AE)
    val codeComment = Color(0xFF6E7781)
    val codeAnnotation = Color(0xFFB35900)
    val codeType = Color(0xFF953800)

    /** Background applied to code spans substituted with the current live value of a knob. */
    val codeHighlight = Color(0x33FDB44B)
}

object CStoriesRadii {
    val shell = 28.dp
    val lg = 20.dp
    val md = 14.dp
    val sm = 10.dp
}
