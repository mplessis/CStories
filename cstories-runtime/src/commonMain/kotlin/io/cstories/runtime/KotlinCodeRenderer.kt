package io.cstories.runtime

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle

/**
 * Renders a Kotlin source snippet (typically a story's [StoryEntry.usageCode],
 * possibly already substituted with live knob values by [substituteKnobValues])
 * as a colorized [AnnotatedString], using [KotlinSyntaxHighlighter]'s
 * hand-rolled tokenizer. [highlightRanges] (character ranges in [code]) get
 * an extra background span layered on top of the syntax colors, marking
 * spans substituted with a knob's current value.
 */
internal fun highlightKotlin(code: String, highlightRanges: List<IntRange> = emptyList()): AnnotatedString =
    buildAnnotatedString {
        KotlinSyntaxHighlighter.tokenize(code).forEach { token ->
            when (token) {
                is KotlinToken.Keyword -> withStyle(SpanStyle(color = CStoriesColors.codeKeyword)) { append(token.text) }
                is KotlinToken.Comment -> withStyle(
                    SpanStyle(color = CStoriesColors.codeComment, fontStyle = FontStyle.Italic),
                ) { append(token.text) }
                is KotlinToken.NumberLiteral -> withStyle(SpanStyle(color = CStoriesColors.codeNumber)) { append(token.text) }
                is KotlinToken.Annotation -> withStyle(SpanStyle(color = CStoriesColors.codeAnnotation)) { append(token.text) }
                is KotlinToken.TypeOrFunctionCall -> withStyle(SpanStyle(color = CStoriesColors.codeType)) { append(token.text) }
                is KotlinToken.Plain -> append(token.text)
                is KotlinToken.StringLiteral -> appendStringLiteral(token)
            }
        }
        highlightRanges.forEach { range ->
            addStyle(SpanStyle(background = CStoriesColors.codeHighlight), range.first, range.last + 1)
        }
    }

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendStringLiteral(token: KotlinToken.StringLiteral) {
    val quote = if (token.text.startsWith("\"\"\"")) "\"\"\"" else "\""
    withStyle(SpanStyle(color = CStoriesColors.codeString)) {
        append(quote)
        token.parts.forEach { part ->
            when (part) {
                is StringPart.Literal -> append(part.text)
                is StringPart.Interpolation -> withStyle(SpanStyle(color = CStoriesColors.codeStringInterpolation)) {
                    append(part.text)
                }
            }
        }
        append(quote)
    }
}
