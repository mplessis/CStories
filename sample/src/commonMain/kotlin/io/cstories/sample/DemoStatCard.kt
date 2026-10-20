package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent
import kotlin.math.abs

/**
 * A compact card highlighting one key metric, e.g. on a dashboard, with an
 * optional trend compared to a previous period.
 *
 * ### Usage
 * - Keep the [label] short and the [value] already formatted ("1,284", "86%", "€12.4k").
 * - Set [trendPercent] to show the evolution: a positive value is shown as an increase,
 *   a negative one as a decrease, `0` as stable.
 * - By default an increase is good (green). Set [higherIsBetter] to `false` for metrics such as
 *   error rates or response times, where an increase is bad (red).
 * - Display stat cards in a row or a grid of 2 to 4 cards with the same width.
 *
 * ### Accessibility
 * - The card is read as a single sentence, e.g. "Coverage: 86%, up 4% versus last period".
 * - The trend arrow is never the only signal: the sign of the percentage is always written.
 *
 * @param label Name of the metric.
 * @param value Formatted value of the metric, displayed large.
 * @param modifier Modifier applied to the card. Use it to set the width.
 * @param trendPercent Evolution in percent compared to the previous period, or `null` to hide the trend.
 * @param higherIsBetter Whether an increase is a positive outcome. Controls the trend color only.
 * @param trendCaption Text displayed after the trend, describing the comparison period.
 */
@CStoryComponent
@Composable
fun DemoStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trendPercent: Int? = null,
    higherIsBetter: Boolean = true,
    trendCaption: String = "vs last period",
) {
    val trendText = trendPercent?.let { "${if (it > 0) "+" else if (it < 0) "−" else ""}${abs(it)}%" }
    val spokenTrend = trendPercent?.let {
        when {
            it > 0 -> "up ${abs(it)}%"
            it < 0 -> "down ${abs(it)}%"
            else -> "stable"
        } + " $trendCaption"
    }
    Card(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = listOfNotNull("$label: $value", spokenTrend).joinToString(", ")
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelMedium)
            Text(value, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.headlineSmall)
            if (trendPercent != null && trendText != null) {
                val isGood = if (trendPercent == 0) null else (trendPercent > 0) == higherIsBetter
                val trendColor = when (isGood) {
                    true -> statusToneColors(StatusTone.Success).accent
                    false -> statusToneColors(StatusTone.Error).accent
                    null -> MaterialTheme.colorScheme.onSecondaryContainer
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (trendPercent != 0) {
                        Icon(
                            imageVector = if (trendPercent > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(trendText, color = trendColor, style = MaterialTheme.typography.labelMedium)
                    Text(
                        trendCaption,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
