package io.cstories.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A centered placeholder displayed when a list, a search or a page has no
 * content yet, explaining why and what to do next.
 *
 * ### Usage
 * - Explain the *reason* in the [title] ("No projects yet", "No results for 'kotlin'").
 * - Use the [description] to tell the user how to fill the space.
 * - Provide an [action] when the user can fix the situation directly, e.g. "Create project".
 *   Leave it empty when there is nothing to do, e.g. an empty notification inbox.
 * - The [icon] is illustrative; prefer a neutral one over an error icon.
 *
 * ### Accessibility
 * - The [title] is marked as a heading.
 * - The [icon] is decorative and not announced.
 *
 * @param title Main sentence explaining why the area is empty.
 * @param modifier Modifier applied to the column. The content is centered horizontally inside it.
 * @param description Optional supporting text below the title.
 * @param icon Optional illustration displayed in a tinted circle above the title.
 * @param action Optional slot below the description, usually a `DemoButton`.
 */
@CStoryComponent
@Composable
fun DemoEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Box(modifier = Modifier.padding(top = 8.dp)) { action() }
        }
    }
}
