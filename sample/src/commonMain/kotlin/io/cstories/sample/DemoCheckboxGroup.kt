package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/**
 * A titled group of checkboxes for picking any number of options, with an
 * optional "Select all" row.
 *
 * ### Usage
 * - Use it when several options can be combined, e.g. notification channels.
 *   For mutually exclusive options, use a `DemoSegmentedControl` instead.
 * - Enable [showSelectAll] from about four options. Its checkbox is *indeterminate*
 *   when only part of the options are selected; clicking it then selects everything.
 * - The component is stateless: keep the [selected] set in the caller.
 *
 * ### Accessibility
 * - Each row is a single checkbox whose accessible name is the option label.
 * - The [title] is marked as a heading so screen-reader users can jump to the group.
 *
 * @param title Heading displayed above the options.
 * @param options Labels of the available options, in display order. Labels must be unique.
 * @param selected Labels of the currently selected options.
 * @param onSelectionChange Called with the full new selection after each click.
 * @param modifier Modifier applied to the group.
 * @param showSelectAll When `true`, adds a tri-state "Select all" row above the options.
 * @param enabled When `false`, every row is greyed out and can't be toggled.
 */
@CStoryComponent
@Composable
fun DemoCheckboxGroup(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    showSelectAll: Boolean = false,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { heading() },
        )
        if (showSelectAll) {
            val selectedCount = options.count { it in selected }
            val state = when (selectedCount) {
                0 -> ToggleableState.Off
                options.size -> ToggleableState.On
                else -> ToggleableState.Indeterminate
            }
            CheckboxRow(
                label = "Select all",
                modifier = Modifier.triStateToggleable(
                    state = state,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onClick = {
                        onSelectionChange(if (state == ToggleableState.On) emptySet() else options.toSet())
                    },
                ),
            ) {
                TriStateCheckbox(state = state, onClick = null, enabled = enabled)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
        options.forEach { option ->
            val isChecked = option in selected
            CheckboxRow(
                label = option,
                modifier = Modifier.toggleable(
                    value = isChecked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { checked ->
                        onSelectionChange(if (checked) selected + option else selected - option)
                    },
                ),
            ) {
                Checkbox(checked = isChecked, onCheckedChange = null, enabled = enabled)
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    modifier: Modifier,
    checkbox: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        checkbox()
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
