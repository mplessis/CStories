package io.cstories.runtime.knobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier

/**
 * Declares the controls for a story. Call this from within a `@CStory`
 * composable, alongside the actual preview:
 *
 * ```
 * @CStory(collection = "DesignSystem", group = "Buttons", name = "Primary")
 * @Composable
 * fun PrimaryButtonStory() {
 *     var label by remember { mutableStateOf("Click me") }
 *     KnobPanel {
 *         TextKnob(label = "Label", value = label, onValueChange = { label = it })
 *     }
 *     PrimaryButton(text = label, onClick = {})
 * }
 * ```
 *
 * When rendered inside [io.cstories.runtime.CStoriesApp], this content is
 * transparently moved into the runtime's dedicated controls panel (so the
 * canvas stage only ever shows the story's actual preview). Outside of that
 * context (e.g. rendering a story standalone), it renders inline instead.
 */
@Composable
fun KnobPanel(content: @Composable ColumnScope.() -> Unit) {
    val slot = LocalControlsSlot.current
    if (slot != null) {
        SideEffect {
            slot.value = { Column(modifier = Modifier.fillMaxWidth(), content = content) }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}
