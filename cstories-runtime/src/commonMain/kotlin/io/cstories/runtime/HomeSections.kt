package io.cstories.runtime

/**
 * Extracts the first paragraph of a component's Markdown documentation, used
 * as a short plain-text summary on the home page (inline `code` and emphasis
 * markers are removed). Returns `null` when the documentation
 * does not start with a plain paragraph (e.g. it starts with a heading, a
 * table or a code block).
 */
internal fun documentationSummary(documentation: String?): String? {
    if (documentation.isNullOrBlank()) return null
    val paragraph = documentation.trim()
        .lineSequence()
        .takeWhile { it.isNotBlank() }
        .map(String::trim)
        .toList()
    val firstLine = paragraph.firstOrNull() ?: return null
    if (firstLine.startsWith("#") || firstLine.startsWith("|") || firstLine.startsWith("```") ||
        firstLine.startsWith("**Parameters**")
    ) {
        return null
    }
    return paragraph.joinToString(" ")
        .replace("`", "")
        .replace("**", "")
        .replace("*", "")
}

/** A group of stories shown as one card on the home page. */
internal data class HomeCard(
    val name: String,
    val summary: String?,
    val storyCount: Int,
    val firstStory: StoryEntry,
)

/** A titled block of the home page: the cards directly under [path], in sidebar order. */
internal data class HomeSection(
    val path: List<String>,
    val cards: List<HomeCard>,
)

/**
 * Flattens [tree] into the home page sections, following the sidebar order:
 * one section per group that directly contains story groups, each story group
 * becoming a card.
 */
internal fun buildHomeSections(tree: List<TreeNode>): List<HomeSection> {
    val sections = mutableListOf<HomeSection>()

    fun visit(group: TreeNode.Group, path: List<String>) {
        val subGroups = group.children.filterIsInstance<TreeNode.Group>()
        val cards = subGroups
            .filter { it.isStoryGroup }
            .map { storyGroup ->
                val stories = storyGroup.children.filterIsInstance<TreeNode.Leaf>().map { it.entry }
                HomeCard(
                    name = storyGroup.name,
                    summary = stories.firstNotNullOfOrNull { documentationSummary(it.documentation) },
                    storyCount = stories.size,
                    firstStory = stories.first(),
                )
            }
        if (cards.isNotEmpty()) sections += HomeSection(path, cards)
        subGroups.filterNot { it.isStoryGroup }.forEach { visit(it, path + it.name) }
    }

    val topLevelGroups = tree.filterIsInstance<TreeNode.Group>()
    val topLevelStoryGroups = TreeNode.Group(name = "", children = topLevelGroups.filter { it.isStoryGroup })
    visit(topLevelStoryGroups, emptyList())
    topLevelGroups.filterNot { it.isStoryGroup }.forEach { visit(it, listOf(it.name)) }
    return sections
}
