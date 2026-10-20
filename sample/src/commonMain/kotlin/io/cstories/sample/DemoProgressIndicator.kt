package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/** Shape of a [DemoProgressIndicator]. */
enum class ProgressStyle {
    /** A horizontal bar. Use it for page-level or file-level progress, where width is available. */
    Linear,

    /** A ring. Use it in compact places such as inside a button or a card. */
    Circular,
}

/**
 * Shows the progress of an ongoing operation, either *determinate*
 * (known percentage) or *indeterminate* (unknown duration).
 *
 * ### Usage
 * - Pass a [progress] between `0` and `100` when the completion is known, e.g. an upload.
 *   Pass `null` for an indeterminate animation when it isn't, e.g. waiting for a server.
 * - Add a [label] describing *what* is in progress ("Uploading report.pdf").
 * - Enable [showPercentage] for long operations so the user can estimate the remaining time.
 * - For operations under one second, don't show any indicator: it would only flicker.
 *
 * ### Accessibility
 * - The indicator exposes its progress to screen readers as a range value.
 * - The [label] is read before the indicator, giving it context.
 *
 * @param progress Completion between `0` and `100`, or `null` for an indeterminate indicator. Values outside the range are clamped.
 * @param modifier Modifier applied to the whole component.
 * @param style Linear bar or circular ring, see [ProgressStyle].
 * @param label Optional text describing the operation, shown above the bar or next to the ring.
 * @param showPercentage When `true` and [progress] is not `null`, shows the percentage as text.
 */
@CStoryComponent
@Composable
fun DemoProgressIndicator(
    progress: Int?,
    modifier: Modifier = Modifier,
    style: ProgressStyle = ProgressStyle.Linear,
    label: String? = null,
    showPercentage: Boolean = false,
) {
    val fraction = progress?.coerceIn(0, 100)?.div(100f)
    val percentText = if (showPercentage && progress != null) "${progress.coerceIn(0, 100)}%" else null

    when (style) {
        ProgressStyle.Linear -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (label != null || percentText != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = label.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (percentText != null) {
                        Text(text = percentText, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (fraction != null) {
                LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        ProgressStyle.Circular -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (fraction != null) {
                    CircularProgressIndicator(progress = { fraction }, modifier = Modifier.size(56.dp))
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(56.dp))
                }
                if (percentText != null) {
                    Text(text = percentText, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (label != null) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
