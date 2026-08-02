package io.cstories.runtime.knobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors
import io.cstories.runtime.CStoriesRadii

/**
 * A dropdown/select-style knob, mirroring the mockup's `<select>` controls
 * (e.g. button `variant`, badge `tone`).
 *
 * [literalValue], when provided, is injected verbatim (unquoted) instead of
 * a quoted string literal when substituting [codeKey] into the story's
 * "Code" tab — used by the [T] enum overload below to inject e.g.
 * `BadgeTone.Success` instead of `"Success"`. Most callers should prefer
 * that overload directly over passing [literalValue] manually.
 */
@Composable
fun SelectKnob(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    codeKey: String? = null,
    literalValue: String? = null,
) {
    PublishKnobValue(codeKey, value, literalValue)
    Knob(label = label) {
        var expanded by remember { mutableStateOf(false) }
        val density = LocalDensity.current
        var anchorWidth by remember { mutableStateOf(0.dp) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        anchorWidth = with(density) { coordinates.size.width.toDp() }
                    }
                    .background(CStoriesColors.surfaceMuted, RoundedCornerShape(CStoriesRadii.sm))
                    .border(1.dp, CStoriesColors.border, RoundedCornerShape(CStoriesRadii.sm))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = value, color = CStoriesColors.text, fontSize = 13.sp)
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = CStoriesColors.textFaint,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = CStoriesColors.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                shape = RoundedCornerShape(CStoriesRadii.sm),
                border = androidx.compose.foundation.BorderStroke(1.dp, CStoriesColors.border),
                modifier = Modifier
                    .width(anchorWidth)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(CStoriesRadii.sm),
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.06f),
                    )
                    .background(CStoriesColors.surface),
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (isSelected) CStoriesColors.primary else CStoriesColors.text,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = CStoriesColors.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = CStoriesColors.text,
                        ),
                        modifier = Modifier.background(
                            if (isSelected) CStoriesColors.primarySoft else CStoriesColors.surface,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Enum-typed overload of [SelectKnob]: derives [options] from `enumValues<T>()`
 * and, when [codeKey] is set, automatically substitutes the story's "Code"
 * tab with the fully-qualified constant (e.g. `BadgeTone.Success`) instead
 * of a quoted string, without the story author needing `.name` / `valueOf`.
 */
@Composable
inline fun <reified T : Enum<T>> SelectKnob(
    label: String,
    value: T,
    noinline onValueChange: (T) -> Unit,
    codeKey: String? = null,
) {
    SelectKnob(
        label = label,
        value = value.name,
        options = enumValues<T>().map { it.name },
        onValueChange = { selected -> onValueChange(enumValueOf<T>(selected)) },
        codeKey = codeKey,
        literalValue = codeKey?.let { "${T::class.simpleName}.${value.name}" },
    )
}
