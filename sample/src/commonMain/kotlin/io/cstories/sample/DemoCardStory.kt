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
    group = "Cards",
    name = "Elevated",
    component = CStoryComponentRefs.DemoCard,
)
@Composable
fun DemoCardElevatedStory() {
    var cardTitle by remember { mutableStateOf("Quarterly report") }
    var cardBody by remember { mutableStateOf("Revenue grew by 12% compared to last quarter, driven by the new subscription plans.") }

    KnobPanel {
        TextKnob(label = "Title", value = cardTitle, onValueChange = { cardTitle = it }, codeKey = "cardTitle")
        TextKnob(label = "Body", value = cardBody, onValueChange = { cardBody = it }, codeKey = "cardBody")
    }

    DemoCard(
        title = cardTitle,
        body = cardBody,
        style = CardStyle.Elevated,
        modifier = Modifier.width(320.dp),
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Cards",
    name = "Outlined with actions",
    component = CStoryComponentRefs.DemoCard,
)
@Composable
fun DemoCardOutlinedStory() {
    var cardTitle by remember { mutableStateOf("Invitation to Design Review") }
    var cardSubtitle by remember { mutableStateOf("Tomorrow, 10:00") }

    KnobPanel {
        TextKnob(label = "Title", value = cardTitle, onValueChange = { cardTitle = it }, codeKey = "cardTitle")
        TextKnob(label = "Subtitle", value = cardSubtitle, onValueChange = { cardSubtitle = it }, codeKey = "cardSubtitle")
    }

    DemoCard(
        title = cardTitle,
        subtitle = cardSubtitle,
        body = "Alex invited you to review the new onboarding screens.",
        style = CardStyle.Outlined,
        modifier = Modifier.width(320.dp),
        actions = {
            DemoButton(text = "Decline", onClick = {}, variant = ButtonVariant.Tertiary)
            DemoButton(text = "Accept", onClick = {})
        },
    )
}

@CStory(
    collection = "DesignSystem",
    group = "Cards",
    name = "Playground",
    component = CStoryComponentRefs.DemoCard,
)
@Composable
fun DemoCardPlaygroundStory() {
    var cardTitle by remember { mutableStateOf("Card title") }
    var cardSubtitle by remember { mutableStateOf("Subtitle") }
    var cardBody by remember { mutableStateOf("A short descriptive text illustrating the card content.") }
    var selectedStyle by remember { mutableStateOf(CardStyle.Filled) }
    var isClickable by remember { mutableStateOf(false) }
    var cardWidth by remember { mutableStateOf(320) }

    KnobPanel {
        TextKnob(label = "Title", value = cardTitle, onValueChange = { cardTitle = it }, codeKey = "cardTitle")
        TextKnob(label = "Subtitle", value = cardSubtitle, onValueChange = { cardSubtitle = it }, codeKey = "cardSubtitle")
        TextKnob(label = "Body", value = cardBody, onValueChange = { cardBody = it }, codeKey = "cardBody")
        SelectKnob(label = "Style", value = selectedStyle, onValueChange = { selectedStyle = it }, codeKey = "selectedStyle")
        BooleanKnob(label = "Clickable", value = isClickable, onValueChange = { isClickable = it })
        IntKnob(label = "Width (dp)", value = cardWidth, onValueChange = { cardWidth = it }, range = 200..480, step = 20, codeKey = "cardWidth")
    }

    DemoCard(
        title = cardTitle,
        subtitle = cardSubtitle,
        body = cardBody,
        style = selectedStyle,
        onClick = if (isClickable) ({}) else null,
        modifier = Modifier.width(cardWidth.dp),
    )
}
