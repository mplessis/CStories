package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.cstories.annotations.CStory
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.TextKnob

@CStory(collection = "DesignSystem", group = "Cards", name = "Elevated")
@Composable
fun ElevatedCardStory() {
    var title by remember { mutableStateOf("Titre de la carte") }
    var body by remember { mutableStateOf("Un court texte descriptif pour illustrer le contenu de la carte.") }
    var elevated by remember { mutableStateOf(true) }

    Column {
        KnobPanel {
            TextKnob(label = "Title", value = title, onValueChange = { title = it })
            TextKnob(label = "Body", value = body, onValueChange = { body = it })
            BooleanKnob(label = "Elevated", value = elevated, onValueChange = { elevated = it })
        }

        DemoCard(title = title, body = body, elevated = elevated)
    }
}
