package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.documentation_panel_title
import org.jetbrains.compose.resources.stringResource

/**
 * Collapsible panel displaying the component usage documentation extracted
 * from the KDoc of the `@CStoryComponent` referenced by the current story
 * (via `@CStory(component = ...)`). Rendered below the [StoryFrame], full
 * width. Collapsed by default.
 */
@Composable
fun ComponentDocumentationPanel(documentation: String, modifier: Modifier = Modifier) {
    var expanded by remember(documentation) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            ChevronIcon(expanded = expanded, tint = CStoriesColors.textFaint)
            Text(
                text = stringResource(Res.string.documentation_panel_title),
                color = CStoriesColors.textMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (expanded) {
            MarkdownText(
                markdown = documentation,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            )
        }
    }
}
