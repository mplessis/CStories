package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStoryComponent

/** Visual emphasis of a [DemoButton], from the strongest to the most discreet. */
enum class ButtonVariant {
    /** Filled with the primary color. Use it for the **single** main action of a screen or dialog. */
    Primary,

    /** Outlined, transparent background. Use it for alternative actions next to a Primary button. */
    Secondary,

    /** Text only, no container. Use it for low-emphasis actions such as "Learn more" or "Skip". */
    Tertiary,

    /** Filled with the error color. Use it for destructive actions such as "Delete". */
    Danger,
}

/** Size of a [DemoButton], controlling its height, padding and typography. */
enum class ButtonSize(
    internal val minHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val iconSize: Dp,
) {
    /** 32dp tall. Use it in dense layouts such as table rows or toolbars. */
    Small(32.dp, 12.dp, 16.dp),

    /** 40dp tall. The default size for most screens. */
    Medium(40.dp, 20.dp, 18.dp),

    /** 52dp tall. Use it for prominent calls to action, e.g. at the bottom of a mobile screen. */
    Large(52.dp, 28.dp, 20.dp),
}

/**
 * The design system's button. It merges the Material 3 filled, outlined
 * and text buttons into one API driven by a [ButtonVariant].
 *
 * ### Usage
 * - Keep **one** `Primary` button per view; use `Secondary` or `Tertiary` for the others.
 * - Use `Danger` only for destructive actions, ideally followed by a confirmation.
 * - Prefer a verb as label ("Save", "Send invitation") rather than "OK".
 * - While [loading] is `true`, the button keeps its size, shows a spinner instead of
 *   the icon and ignores clicks, so a request can't be submitted twice.
 *
 * ### Accessibility
 * - The [text] is the accessible name; the [leadingIcon] is decorative and has no content description.
 * - When [loading] is `true`, the button announces a "Loading" state description.
 * - Every size keeps a touch target of at least 48dp through Material's minimum interactive size.
 *
 * ### Example
 * A form footer with a secondary "Cancel" action and a primary "Save" action that shows a
 * spinner while the request is running:
 *
 * ```kotlin
 * var isSaving by remember { mutableStateOf(false) }
 * val scope = rememberCoroutineScope()
 *
 * Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
 *     DemoButton(
 *         text = "Cancel",
 *         onClick = onCancel,
 *         variant = ButtonVariant.Secondary,
 *         enabled = !isSaving,
 *     )
 *     DemoButton(
 *         text = "Save",
 *         onClick = {
 *             scope.launch {
 *                 isSaving = true
 *                 repository.save(draft)
 *                 isSaving = false
 *             }
 *         },
 *         loading = isSaving,
 *         leadingIcon = Icons.Filled.Check,
 *     )
 * }
 * ```
 *
 * @param text Label shown inside the button. Keep it short (1–3 words).
 * @param onClick Called when the user clicks the button. Not called while [enabled] is `false` or [loading] is `true`.
 * @param modifier Modifier applied to the button container.
 * @param variant Visual emphasis of the button. See [ButtonVariant].
 * @param size Height, padding and icon size. See [ButtonSize].
 * @param enabled When `false`, the button is greyed out and does not react to clicks.
 * @param loading When `true`, replaces the leading icon with a progress spinner and disables clicks while keeping the enabled colors.
 * @param leadingIcon Optional icon displayed before the [text]. Pass `null` for a text-only button.
 * @param fullWidth When `true`, the button fills the available width. Useful for mobile forms and dialogs.
 */
@CStoryComponent
@Composable
fun DemoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = false,
) {
    val buttonModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .heightIn(min = size.minHeight)
        .semantics { if (loading) stateDescription = "Loading" }
    val contentPadding = PaddingValues(horizontal = size.horizontalPadding)
    val onClickGuarded = { if (!loading) onClick() }
    val content: @Composable () -> Unit = {
        ButtonContent(text = text, size = size, loading = loading, leadingIcon = leadingIcon)
    }

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClickGuarded,
            modifier = buttonModifier,
            enabled = enabled,
            contentPadding = contentPadding,
        ) { content() }

        ButtonVariant.Danger -> Button(
            onClick = onClickGuarded,
            modifier = buttonModifier,
            enabled = enabled,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) { content() }

        ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClickGuarded,
            modifier = buttonModifier,
            enabled = enabled,
            contentPadding = contentPadding,
        ) { content() }

        ButtonVariant.Tertiary -> TextButton(
            onClick = onClickGuarded,
            modifier = buttonModifier,
            enabled = enabled,
            contentPadding = contentPadding,
        ) { content() }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    size: ButtonSize,
    loading: Boolean,
    leadingIcon: ImageVector?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )

            leadingIcon != null -> Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(size.iconSize),
            )
        }
        Text(text = text, style = size.textStyle())
    }
}

@Composable
private fun ButtonSize.textStyle(): TextStyle = when (this) {
    ButtonSize.Small -> MaterialTheme.typography.labelMedium
    ButtonSize.Medium -> MaterialTheme.typography.labelLarge
    ButtonSize.Large -> MaterialTheme.typography.titleMedium
}
