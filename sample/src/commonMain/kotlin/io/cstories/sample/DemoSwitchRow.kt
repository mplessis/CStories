package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A settings row made of a title, an optional description and a trailing
 * switch. The whole row is the touch target, not only the switch.
 *
 * ### Usage
 * - Use it for settings that apply **immediately** ("Dark mode", "Notifications").
 *   For choices validated by a "Save" button, prefer a checkbox.
 * - Phrase the [title] positively so `on` means "enabled": "Show previews", not "Hide previews".
 * - Use the [description] to explain the consequence of the setting in one sentence.
 *
 * ### Accessibility
 * - The row is announced as a single switch whose name is the [title], with its on/off state.
 * - The inner switch is not focusable on its own, so keyboard users get one tab stop per row.
 *
 * @param title Name of the setting, also used as the accessible name.
 * @param checked Whether the setting is on.
 * @param onCheckedChange Called with the new value when the row is clicked.
 * @param modifier Modifier applied to the row.
 * @param description Optional secondary text below the title.
 * @param enabled When `false`, the row is greyed out and can't be toggled.
 */
@CStoryComponent
@Composable
fun DemoSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
) {
    val contentAlpha = if (enabled) 1f else 0.38f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
