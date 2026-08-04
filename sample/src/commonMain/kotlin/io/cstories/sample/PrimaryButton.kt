package io.cstories.sample

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.cstories.annotations.CStoryComponent

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
