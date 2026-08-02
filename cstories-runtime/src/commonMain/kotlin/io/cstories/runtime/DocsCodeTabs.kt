package io.cstories.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private enum class DocsCodeTab { DOCS, CODE }

/**
 * Tabbed panel rendered below the [StoryFrame], full width, switching
 * between the component's usage documentation ([documentation], extracted
 * from its `@CStoryComponent` KDoc) and the exact source snippet that
 * invokes it ([usageCode], extracted from the story's own source).
 *
 * A tab only appears when its content is available. Renders nothing when
 * both are `null` (e.g. the story has no `component = ...` reference).
 */
@Composable
fun DocsCodeTabs(
    documentation: String?,
    usageCode: String?,
    knobValues: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    if (documentation == null && usageCode == null) return

    var selected by remember(documentation, usageCode) {
        mutableStateOf(if (documentation != null) DocsCodeTab.DOCS else DocsCodeTab.CODE)
    }
    var expanded by remember(documentation, usageCode) { mutableStateOf(false) }
    val substitution = remember(usageCode, knobValues) {
        usageCode?.let { substituteKnobValues(it, knobValues) }
    }
    val collapseDescription = stringResource(Res.string.docs_code_panel_collapse)
    val expandDescription = stringResource(Res.string.docs_code_panel_expand)
    val copyDescription = stringResource(Res.string.code_copy_button_description)
    val copiedDescription = stringResource(Res.string.code_copy_button_copied_description)
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember(usageCode, knobValues) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (documentation != null) {
                DocsCodeTabButton(
                    label = stringResource(Res.string.docs_tab_label),
                    selected = selected == DocsCodeTab.DOCS,
                    onClick = {
                        selected = DocsCodeTab.DOCS
                        expanded = true
                    },
                )
            }
            if (usageCode != null) {
                DocsCodeTabButton(
                    label = stringResource(Res.string.code_tab_label),
                    selected = selected == DocsCodeTab.CODE,
                    onClick = {
                        selected = DocsCodeTab.CODE
                        expanded = true
                    },
                )
            }
            Spacer(Modifier.weight(1f))
            ChevronIcon(
                expanded = expanded,
                tint = CStoriesColors.textFaint,
                modifier = Modifier
                    .clip(RoundedCornerShape(CStoriesRadii.sm))
                    .clickable(onClick = { expanded = !expanded })
                    .semantics {
                        contentDescription = if (expanded) collapseDescription else expandDescription
                    }
                    .padding(6.dp),
            )
        }
        if (expanded) {
            when (selected) {
                DocsCodeTab.DOCS -> documentation?.let {
                    MarkdownText(
                        markdown = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    )
                }

                DocsCodeTab.CODE -> substitution?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    ) {
                        CodeBlock(
                            code = it.text,
                            highlightRanges = it.highlightRanges,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(it.text))
                                copied = true
                                scope.launch {
                                    delay(1500)
                                    copied = false
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .semantics {
                                    contentDescription = if (copied) copiedDescription else copyDescription
                                },
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = CStoriesColors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocsCodeTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) CStoriesColors.text else CStoriesColors.textFaint,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(CStoriesRadii.sm))
            .clickable(onClick = onClick)
            .background(
                if (selected) CStoriesColors.surfaceMuted else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(CStoriesRadii.sm),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
