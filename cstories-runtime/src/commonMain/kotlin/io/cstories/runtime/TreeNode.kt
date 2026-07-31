package io.cstories.runtime

sealed interface TreeNode {
    val name: String

    data class Group(
        override val name: String,
        val children: List<TreeNode>,
    ) : TreeNode

    data class Leaf(
        override val name: String,
        val entry: StoryEntry,
    ) : TreeNode
}

fun buildTree(entries: List<StoryEntry>): List<TreeNode> {
    if (entries.isEmpty()) {
        return emptyList()
    }

    fun buildLevel(levelEntries: List<StoryEntry>, depth: Int): List<TreeNode> {
        return levelEntries
            .sortedBy { it.path.joinToString("/") }
            .groupBy { it.path.getOrNull(depth) }
            .entries
            .sortedBy { it.key }
            .mapNotNull { (segment, groupedEntries) ->
                if (segment == null) {
                    return@mapNotNull null
                }

                val isLeafLevel = groupedEntries.all { it.path.lastIndex == depth }
                if (isLeafLevel) {
                    TreeNode.Leaf(
                        name = segment,
                        entry = groupedEntries.first(),
                    )
                } else {
                    TreeNode.Group(
                        name = segment,
                        children = buildLevel(groupedEntries, depth + 1),
                    )
                }
            }
    }

    return buildLevel(entries, depth = 0)
}
