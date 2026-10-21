package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.home_components_title
import io.cstories.runtime.resources.home_no_match
import io.cstories.runtime.resources.home_story_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Catalog home page: the optional `CSTORIES.md` introduction, then every story
 * group of [tree] as a card, organised in sections following the sidebar.
 */
@Composable
internal fun HomePage(
    tree: List<TreeNode>,
    homeMarkdown: String?,
    onOpenStory: (StoryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = remember(tree) { buildHomeSections(tree) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (!homeMarkdown.isNullOrBlank()) {
            MarkdownText(
                markdown = homeMarkdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CStoriesRadii.lg))
                    .background(CStoriesColors.surface)
                    .padding(24.dp),
            )
        }
        Text(
            text = stringResource(Res.string.home_components_title),
            color = CStoriesColors.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.semantics { heading() },
        )
        if (sections.isEmpty()) {
            Text(stringResource(Res.string.home_no_match), color = CStoriesColors.textMuted, fontSize = 13.sp)
        }
        sections.forEach { section -> HomeSectionBlock(section, onOpenStory) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSectionBlock(section: HomeSection, onOpenStory: (StoryEntry) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (section.path.isNotEmpty()) {
            Breadcrumb(path = section.path, modifier = Modifier.semantics { heading() })
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            section.cards.forEach { card -> HomeCardItem(card, onClick = { onOpenStory(card.firstStory) }) }
        }
    }
}

@Composable
private fun HomeCardItem(card: HomeCard, onClick: () -> Unit) {
    val shape = RoundedCornerShape(CStoriesRadii.md)
    Column(
        modifier = Modifier
            .width(280.dp)
            .clip(shape)
            .background(CStoriesColors.surface)
            .border(1.dp, CStoriesColors.border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = card.name,
            color = CStoriesColors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (card.summary != null) {
            Text(
                text = card.summary,
                color = CStoriesColors.textMuted,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = pluralStringResource(Res.plurals.home_story_count, card.storyCount, card.storyCount),
            color = CStoriesColors.textFaint,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
