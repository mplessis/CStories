package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.cstories.annotations.CStoryComponent

/**
 * The bar displayed at the top of a screen, with its title, an optional
 * back button and up to three actions.
 *
 * ### Usage
 * - Use the [title] to tell the user where they are; keep it short, it is truncated on one line.
 * - Provide [onBack] on every screen that is not a root destination.
 * - Put at most three icon buttons in [actions], ordered by importance from the right edge.
 *   Move the others to an overflow menu.
 * - Set [subtitle] for secondary context, e.g. the parent project of the current page.
 *
 * ### Accessibility
 * - The back button is labeled "Navigate back".
 * - Each action icon in [actions] must have a `contentDescription`.
 *
 * @param title Name of the current screen.
 * @param modifier Modifier applied to the bar.
 * @param subtitle Optional line displayed under the title in a smaller style.
 * @param onBack When not `null`, shows a back arrow at the start that calls this lambda.
 * @param actions Optional slot for icon buttons displayed at the end of the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@CStoryComponent
@Composable
fun DemoTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    )
}
