package io.cstories.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A row of a list, made of a leading element, a title, an optional
 * subtitle and a trailing element.
 *
 * ### Usage
 * - Use [leading] for something that identifies the item: a `DemoAvatar` or an icon.
 * - Use [trailing] for metadata or a secondary control: a `DemoBadge`, a timestamp, a chevron or a switch.
 * - When [onClick] is set, the whole row is clickable; don't put a second clickable element in it
 *   except in [trailing].
 * - The [title] and [subtitle] are limited to one line each and ellipsized.
 *
 * ### Accessibility
 * - A clickable row is announced as a button, its name merging the title and subtitle.
 * - The row is at least 56dp tall, above the 48dp minimum touch target.
 *
 * @param title Primary text of the row.
 * @param modifier Modifier applied to the row.
 * @param subtitle Optional secondary text below the title.
 * @param leading Optional slot at the start of the row, vertically centered.
 * @param trailing Optional slot at the end of the row, vertically centered.
 * @param onClick When not `null`, makes the whole row clickable.
 */
@CStoryComponent
@Composable
fun DemoListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}
