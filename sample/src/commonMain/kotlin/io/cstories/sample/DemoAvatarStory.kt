package io.cstories.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs
import io.cstories.runtime.knobs.KnobPanel
import io.cstories.runtime.knobs.SelectKnob
import io.cstories.runtime.knobs.TextKnob

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "Avatar",
    component = CStoryComponentRefs.DemoAvatar,
)
@Composable
fun DemoAvatarStory() {
    var personName by remember { mutableStateOf("Ada Lovelace") }
    var selectedSize by remember { mutableStateOf(AvatarSize.Large) }
    var selectedShape by remember { mutableStateOf(AvatarShape.Circle) }
    var selectedPresence by remember { mutableStateOf(Presence.Online) }

    KnobPanel {
        TextKnob(label = "Name", value = personName, onValueChange = { personName = it }, codeKey = "personName")
        SelectKnob(label = "Size", value = selectedSize, onValueChange = { selectedSize = it }, codeKey = "selectedSize")
        SelectKnob(label = "Shape", value = selectedShape, onValueChange = { selectedShape = it }, codeKey = "selectedShape")
        SelectKnob(label = "Presence", value = selectedPresence, onValueChange = { selectedPresence = it }, codeKey = "selectedPresence")
    }

    DemoAvatar(
        name = personName,
        size = selectedSize,
        shape = selectedShape,
        presence = selectedPresence,
    )
}

@CStory(
    collection = "DesignSystem",
    group = "DataDisplay",
    name = "Avatar sizes",
    component = CStoryComponentRefs.DemoAvatar,
)
@Composable
fun DemoAvatarSizesStory() {
    var selectedPresence by remember { mutableStateOf(Presence.Hidden) }

    KnobPanel {
        SelectKnob(label = "Presence", value = selectedPresence, onValueChange = { selectedPresence = it }, codeKey = "selectedPresence")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        AvatarSize.entries.forEach { avatarSize ->
            DemoAvatar(name = "Grace Hopper", size = avatarSize, presence = selectedPresence)
        }
    }
}
