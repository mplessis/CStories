package io.cstories.sample

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.cstories.annotations.CStoryComponent

/**
 * A horizontal row of 2 to 5 connected buttons where exactly one option is
 * selected, e.g. "Day / Week / Month". Built on Material 3's segmented buttons.
 *
 * ### Usage
 * - Use it to switch between views or filters of the *same* content.
 *   To navigate between different sections, use `DemoTabs`.
 * - Keep labels short (one word) and the options at five at most; beyond that use a dropdown.
 * - The control is generic: pass any list of values and a [labelOf] function to display them,
 *   e.g. the entries of an enum.
 *
 * ### Accessibility
 * - Each segment is announced as a radio button with its label and selected state.
 * - The selected segment also shows a check mark, so the selection isn't conveyed by color only.
 *
 * @param options The values to choose from, in display order.
 * @param selected The currently selected value. Must be one of [options].
 * @param onSelectedChange Called with the clicked value.
 * @param modifier Modifier applied to the row.
 * @param labelOf Converts a value to its displayed label. Defaults to `toString()`.
 * @param enabled When `false`, every segment is greyed out and can't be selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@CStoryComponent
@Composable
fun <T> DemoSegmentedControl(
    options: List<T>,
    selected: T,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelOf: (T) -> String = { it.toString() },
    enabled: Boolean = true,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelectedChange(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
            ) {
                Text(labelOf(option))
            }
        }
    }
}
