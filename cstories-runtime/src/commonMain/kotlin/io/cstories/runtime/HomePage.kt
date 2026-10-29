package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.home_collection_collapse
import io.cstories.runtime.resources.home_collection_expand
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
    val collections = remember(tree) { buildHomeCollections(tree) }
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
        if (collections.isEmpty()) {
            Text(stringResource(Res.string.home_no_match), color = CStoriesColors.textMuted, fontSize = 13.sp)
        }
        collections.forEach { collection -> HomeCollectionBlock(collection, onOpenStory) }
    }
}

@Composable
private fun HomeCollectionBlock(collection: HomeCollection, onOpenStory: (StoryEntry) -> Unit) {
    if (collection.name.isEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            collection.sections.forEach { section -> HomeSectionBlock(section, onOpenStory) }
        }
        return
    }
    var expanded by remember(collection.name) { mutableStateOf(true) }
    val stateDescription = stringResource(
        if (expanded) Res.string.home_collection_collapse else Res.string.home_collection_expand,
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CStoriesRadii.sm))
                .clickable(onClick = { expanded = !expanded })
                .semantics {
                    heading()
                    contentDescription = stateDescription
                }
                .padding(vertical = 6.dp),
        ) {
            ChevronIcon(expanded = expanded, tint = CStoriesColors.textMuted)
            Text(
                text = collection.name,
                color = CStoriesColors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = pluralStringResource(Res.plurals.home_story_count, collection.storyCount, collection.storyCount),
                color = CStoriesColors.textFaint,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                collection.sections.forEach { section -> HomeSectionBlock(section, onOpenStory) }
            }
        }
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
