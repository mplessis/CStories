package io.cstories.sample

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import io.cstories.annotations.CStoryComponent

/**
 * A single text input with a floating label, helper text, error state and
 * an optional character counter. Built on Material 3's `OutlinedTextField`.
 *
 * ### Usage
 * - Always provide a [label]; the [placeholder] is only an example of the expected value.
 * - Use [supportingText] to explain the format ("At least 8 characters") *before* the user makes a mistake.
 * - Set [isError] and [errorMessage] after validation; the message replaces the supporting text.
 * - Set [maxLength] to cap the input; a "12 / 40" counter is then shown at the end of the helper line
 *   and extra characters are ignored.
 *
 * ### Accessibility
 * - The [label] is the accessible name of the field.
 * - When [isError] is `true`, the [errorMessage] is exposed to screen readers as the field's error.
 * - Never rely on the red color alone: always provide an [errorMessage].
 *
 * ### Example
 * An email field whose state is hoisted in the caller and validated on every change:
 *
 * ```kotlin
 * var email by remember { mutableStateOf("") }
 * val isInvalid = email.isNotEmpty() && "@" !in email
 *
 * DemoTextField(
 *     value = email,
 *     onValueChange = { email = it },
 *     label = "Email",
 *     placeholder = "jane@example.com",
 *     supportingText = "We'll never share your email.",
 *     isError = isInvalid,
 *     errorMessage = "Enter a valid email address",
 *     maxLength = 80,
 * )
 * ```
 *
 * @param value Current text of the field. The component is stateless: hoist this value in the caller.
 * @param onValueChange Called with the new text on every keystroke, already truncated to [maxLength].
 * @param label Floating label, also used as the accessible name.
 * @param modifier Modifier applied to the text field.
 * @param placeholder Hint shown when the field is empty and focused.
 * @param supportingText Helper text below the field. Hidden while an [errorMessage] is shown.
 * @param leadingIcon Optional icon displayed at the start of the field, e.g. a search glass.
 * @param isError When `true`, the field uses the error colors.
 * @param errorMessage Message shown below the field when [isError] is `true`.
 * @param enabled When `false`, the field is greyed out and can't be focused.
 * @param singleLine When `true`, the field doesn't wrap and the Enter key doesn't add a new line.
 * @param maxLength Maximum number of characters, or `null` for no limit and no counter.
 */
@CStoryComponent
@Composable
fun DemoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLength: Int? = null,
) {
    val helperText = if (isError && !errorMessage.isNullOrBlank()) errorMessage else supportingText
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(if (maxLength != null) newValue.take(maxLength) else newValue)
        },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        supportingText = if (helperText != null || maxLength != null) {
            {
                Row {
                    Text(text = helperText.orEmpty(), modifier = Modifier.weight(1f))
                    if (maxLength != null) Text("${value.length} / $maxLength")
                }
            }
        } else {
            null
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (isError && !errorMessage.isNullOrBlank()) error(errorMessage) },
    )
}
