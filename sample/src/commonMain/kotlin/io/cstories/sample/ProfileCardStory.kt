package io.cstories.sample

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.BooleanKnob
import io.cstories.runtime.knobs.IntKnob
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "Patterns",
    name = "Profile card",
    component = CStoryComponentRefs.ProfileCard,
)
@Composable
fun ProfileCardStory() {
    var personName by remember { mutableStateOf("Grace Hopper") }
    var personRole by remember { mutableStateOf("Admin") }
    var personBio by remember { mutableStateOf("Compiler pioneer. Leads the platform team and reviews every release.") }
    var selectedPresence by remember { mutableStateOf(Presence.Online) }
    var selectedTone by remember { mutableStateOf(StatusTone.Info) }
    var showSecondary by remember { mutableStateOf(true) }
    var cardWidth by remember { mutableStateOf(320) }

    KnobPanel {
        TextKnob(label = "Name", value = personName, onValueChange = { personName = it }, codeKey = "personName")
        TextKnob(label = "Role", value = personRole, onValueChange = { personRole = it }, codeKey = "personRole")
        TextKnob(label = "Bio", value = personBio, onValueChange = { personBio = it }, codeKey = "personBio")
        SelectKnob(label = "Presence", value = selectedPresence, onValueChange = { selectedPresence = it }, codeKey = "selectedPresence")
        SelectKnob(label = "Role tone", value = selectedTone, onValueChange = { selectedTone = it }, codeKey = "selectedTone")
        BooleanKnob(label = "Secondary action", value = showSecondary, onValueChange = { showSecondary = it })
        IntKnob(label = "Width (dp)", value = cardWidth, onValueChange = { cardWidth = it }, range = 240..400, step = 20, codeKey = "cardWidth")
    }

    ProfileCard(
        name = personName,
        role = personRole,
        bio = personBio,
        presence = selectedPresence,
        roleTone = selectedTone,
        secondaryActionLabel = if (showSecondary) "View profile" else null,
        modifier = Modifier.width(cardWidth.dp),
    )
}
