package io.cstories.runtime

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.promo_copied_button
import io.cstories.runtime.resources.promo_description
import io.cstories.runtime.resources.promo_title
import io.cstories.runtime.resources.promo_watch_button
import io.cstories.runtime.resources.sidebar_search_placeholder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun Sidebar(
    tree: List<TreeNode>,
    selectedPath: List<String>?,
    onSelect: (StoryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .background(CStoriesColors.surface)
            .padding(horizontal = 14.dp, vertical = 20.dp),
    ) {
        BrandRow()
        Spacer(Modifier.height(18.dp))
        SearchField(
            value = query,
            onValueChange = { query = it },
        )
        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            tree.forEach { node ->
                if (query.isBlank() || nodeMatches(node, query)) {
                    TreeNodeItem(
                        node = node,
                        selectedPath = selectedPath,
                        onSelect = onSelect,
                        depth = 0,
                        query = query,
                    )
                }
            }
        }
        if (!isHotReloadActive()) {
            Spacer(Modifier.height(14.dp))
            PromoCard()
        }
    }
}

private fun nodeMatches(node: TreeNode, query: String): Boolean {
    val needle = query.trim().lowercase()
    return needle.isEmpty() || when (node) {
        is TreeNode.Leaf -> node.name.lowercase().contains(needle)
        is TreeNode.Group -> node.children.any { nodeMatches(it, needle) }
    }
}

@Composable
private fun BrandRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(CStoriesColors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "C",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Text(
            text = "CStories",
            color = CStoriesColors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .background(CStoriesColors.surfaceMuted)
            .border(1.dp, CStoriesColors.border, RoundedCornerShape(CStoriesRadii.sm))
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = stringResource(Res.string.sidebar_search_placeholder),
                color = CStoriesColors.textFaint,
                fontSize = 13.sp,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = CStoriesColors.text, fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun TreeNodeItem(
    node: TreeNode,
    selectedPath: List<String>?,
    onSelect: (StoryEntry) -> Unit,
    depth: Int,
    query: String,
) {
    when (node) {
        is TreeNode.Group -> {
            var expanded by remember(node.name, depth) { mutableStateOf(true) }
            if (depth == 0) {
                CollectionHeaderRow(
                    name = node.name,
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                )
            } else {
                GroupHeaderRow(
                    name = node.name,
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                    depth = depth,
                )
            }
            if (expanded) {
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(CStoriesColors.borderSoft),
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        node.children.forEach { child ->
                            if (query.isBlank() || nodeMatches(child, query)) {
                                TreeNodeItem(
                                    node = child,
                                    selectedPath = selectedPath,
                                    onSelect = onSelect,
                                    depth = depth + 1,
                                    query = query,
                                )
                            }
                        }
                    }
                }
            }
        }

        is TreeNode.Leaf -> {
            val isSelected = node.entry.path == selectedPath
            LeafRow(
                name = node.name,
                isSelected = isSelected,
                onClick = { onSelect(node.entry) },
            )
        }
    }
}

@Composable
private fun CollectionHeaderRow(
    name: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        ChevronIcon(expanded = expanded, tint = CStoriesColors.textFaint)
        CollectionIcon(tint = CStoriesColors.textFaint)
        Text(
            text = name,
            color = CStoriesColors.textFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
        )
    }
}

@Composable
private fun GroupHeaderRow(
    name: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    depth: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        ChevronIcon(expanded = expanded, tint = CStoriesColors.textFaint)
        GroupIcon(tint = CStoriesColors.primary)
        Text(
            text = name,
            color = CStoriesColors.textMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LeafRow(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .background(if (isSelected) CStoriesColors.primarySoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isSelected) CStoriesColors.primary else CStoriesColors.border),
        )
        Text(
            text = name,
            color = if (isSelected) CStoriesColors.primary else CStoriesColors.textMuted,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun PromoCard() {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    val command = hotReloadCommand

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CStoriesRadii.md))
            .background(CStoriesColors.dark)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.promo_title),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.promo_description, command),
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                clipboard.setText(AnnotatedString(command))
                copied = true
                scope.launch {
                    delay(1500)
                    copied = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CStoriesColors.primary),
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(vertical = 9.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (copied) stringResource(Res.string.promo_copied_button) else stringResource(Res.string.promo_watch_button),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
