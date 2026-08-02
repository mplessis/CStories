package io.cstories.sample

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.cstories.annotations.CStoryComponent

/**
 * High-emphasis filled button with a dark background.
 *
 * @param onClick Called when the button is clicked.
 * @param size Controls the button height, padding, icon size, and text style.
 * @param enabled Whether the button is interactive.
 * @param label Optional text label displayed inside the button.
 * @param adornment Optional icon or spinner displayed with the label.
 * @param adornmentPosition Position to display adornment before or after the label.
 * @param fillWidth Whether the button should fill the available width. Defaults to `true` on mobile, `false` on desktop.
 */
@CStoryComponent
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(text, color = Color.Yellow)
    }
}
