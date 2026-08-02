package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Read-only monospace rendering of a Kotlin source snippet, with Kotlin
 * syntax highlighting (see [KotlinSyntaxHighlighter]), used to display
 * the exact expression that invokes a story's referenced component (see
 * [DocsCodeTabs]).
 */
@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    val highlighted = remember(code) { highlightKotlin(code) }
    Text(
        text = highlighted,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        color = CStoriesColors.text,
        modifier = modifier
            .background(CStoriesColors.surfaceMuted, RoundedCornerShape(CStoriesRadii.md))
            .padding(14.dp),
    )
}
