package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/** Visual style of a [DemoCard]. */
enum class CardStyle {
    /** Raised with a shadow. Use it to make a card stand out from a flat background. */
    Elevated,

    /** Flat with a thin border. Use it in lists or grids of many cards, where shadows get noisy. */
    Outlined,

    /** Flat with a tinted container. Use it to group content inside an already elevated surface. */
    Filled,
}

/**
 * A container that groups a title, an optional subtitle, a body text and
 * optional actions about a single subject.
 *
 * ### Usage
 * - Use cards for content that can be scanned and compared, e.g. a list of projects.
 * - When [onClick] is provided, the whole card is clickable: don't also put a
 *   button that does the same thing in [actions].
 * - The card has no fixed width. Size it from the caller with [modifier].
 *
 * ### Accessibility
 * - A clickable card is announced as a button; its accessible name comes from the texts it contains.
 * - Keep [actions] to two buttons at most so keyboard users don't have to tab through too many.
 *
 * @param title Main heading of the card, in `titleMedium` style.
 * @param body Supporting text below the title.
 * @param modifier Modifier applied to the card container. Use it to set the width.
 * @param subtitle Optional short line between the title and the body, e.g. a date or an author.
 * @param style Visual style of the card, see [CardStyle].
 * @param onClick When not `null`, the whole card becomes clickable and calls this lambda.
 * @param actions Optional slot rendered as a right-aligned row at the bottom, usually one or two `DemoButton`s.
 */
@CStoryComponent
@Composable
fun DemoCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: CardStyle = CardStyle.Elevated,
    onClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (actions != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    content = actions,
                )
            }
        }
    }

    when (style) {
        CardStyle.Elevated -> if (onClick != null) {
            ElevatedCard(onClick = onClick, modifier = modifier) { content() }
        } else {
            ElevatedCard(modifier = modifier) { content() }
        }

        CardStyle.Outlined -> if (onClick != null) {
            OutlinedCard(onClick = onClick, modifier = modifier) { content() }
        } else {
            OutlinedCard(modifier = modifier) { content() }
        }

        CardStyle.Filled -> if (onClick != null) {
            Card(onClick = onClick, modifier = modifier) { content() }
        } else {
            Card(modifier = modifier) { content() }
        }
    }
}
