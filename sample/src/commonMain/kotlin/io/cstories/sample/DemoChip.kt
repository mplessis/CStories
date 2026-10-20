package io.cstories.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.cstories.annotations.CStoryComponent

/** Behavior of a [DemoChip]. */
enum class ChipType {
    /** Toggles a filter on and off. Shows a check mark when selected. */
    Filter,

    /** Represents a value entered by the user, e.g. a recipient or a tag. Can be removed with its close icon. */
    Input,
}

/**
 * A small interactive element representing a filter or an entered value.
 *
 * ### Usage
 * - Use [ChipType.Filter] in a row of chips to refine content ("Open", "Assigned to me").
 *   Several filter chips can be selected at the same time.
 * - Use [ChipType.Input] for values the user added, such as email recipients or tags. Provide
 *   [onRemove] so the user can delete them.
 * - To show a status that is *not* interactive, use a `DemoBadge` instead.
 *
 * ### Accessibility
 * - Filter chips are announced with their selected state.
 * - The remove icon of input chips is labeled "Remove <text>".
 *
 * @param text Label of the chip.
 * @param modifier Modifier applied to the chip.
 * @param type Filter or input behavior, see [ChipType].
 * @param selected Whether the chip is selected. Filter chips show a check mark when selected.
 * @param onClick Called when the chip is clicked, usually to toggle [selected].
 * @param onRemove For [ChipType.Input] chips, called when the close icon is clicked. When `null`, no close icon is shown.
 * @param leadingIcon Optional icon displayed before the [text]. For filter chips it is replaced by the check mark while selected.
 * @param enabled When `false`, the chip is greyed out and doesn't react to clicks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@CStoryComponent
@Composable
fun DemoChip(
    text: String,
    modifier: Modifier = Modifier,
    type: ChipType = ChipType.Filter,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    when (type) {
        ChipType.Filter -> FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = when {
                selected -> {
                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                }
                leadingIcon != null -> {
                    { Icon(leadingIcon, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                }
                else -> null
            },
        )

        ChipType.Input -> InputChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, Modifier.size(InputChipDefaults.IconSize)) }
            },
            trailingIcon = onRemove?.let { remove ->
                {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove $text",
                        modifier = Modifier.size(InputChipDefaults.IconSize).clickable(enabled = enabled, onClick = remove),
                    )
                }
            },
        )
    }
}
