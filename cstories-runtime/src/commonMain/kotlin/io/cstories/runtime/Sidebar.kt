package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(
    tree: List<TreeNode>,
    selectedPath: List<String>?,
    onSelect: (StoryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(tree, key = { it.name + selectedPath.orEmpty().joinToString("/") }) { node ->
            TreeNodeItem(
                node = node,
                selectedPath = selectedPath,
                onSelect = onSelect,
                indent = 0,
            )
        }
    }
}

@Composable
private fun TreeNodeItem(
    node: TreeNode,
    selectedPath: List<String>?,
    onSelect: (StoryEntry) -> Unit,
    indent: Int,
) {
    when (node) {
        is TreeNode.Group -> {
            var expanded by remember(node.name) { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(start = (indent * 12).dp, top = 8.dp, bottom = 8.dp),
            ) {
                Text(if (expanded) "v ${node.name}" else "> ${node.name}")
            }
            if (expanded) {
                node.children.forEach { child ->
                    TreeNodeItem(
                        node = child,
                        selectedPath = selectedPath,
                        onSelect = onSelect,
                        indent = indent + 1,
                    )
                }
            }
        }

        is TreeNode.Leaf -> {
            val isSelected = node.entry.path == selectedPath
            Text(
                text = node.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onSelect(node.entry) }
                    .padding(start = (indent * 12).dp, top = 8.dp, bottom = 8.dp),
            )
        }
    }
}
