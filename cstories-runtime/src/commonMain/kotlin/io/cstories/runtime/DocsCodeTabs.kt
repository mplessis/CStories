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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.code_tab_label
import io.cstories.runtime.resources.docs_tab_label
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
fun DocsCodeTabs(documentation: String?, usageCode: String?, modifier: Modifier = Modifier) {
    if (documentation == null && usageCode == null) return

    var selected by remember(documentation, usageCode) {
        mutableStateOf(if (documentation != null) DocsCodeTab.DOCS else DocsCodeTab.CODE)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CStoriesColors.surface, RoundedCornerShape(CStoriesRadii.lg))
            .border(1.dp, CStoriesColors.borderSoft, RoundedCornerShape(CStoriesRadii.lg)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (documentation != null) {
                DocsCodeTabButton(
                    label = stringResource(Res.string.docs_tab_label),
                    selected = selected == DocsCodeTab.DOCS,
                    onClick = { selected = DocsCodeTab.DOCS },
                )
            }
            if (usageCode != null) {
                DocsCodeTabButton(
                    label = stringResource(Res.string.code_tab_label),
                    selected = selected == DocsCodeTab.CODE,
                    onClick = { selected = DocsCodeTab.CODE },
                )
            }
        }
        when (selected) {
            DocsCodeTab.DOCS -> documentation?.let {
                MarkdownText(
                    markdown = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                )
            }

            DocsCodeTab.CODE -> usageCode?.let {
                CodeBlock(
                    code = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                )
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
            .clickable(onClick = onClick)
            .background(
                if (selected) CStoriesColors.surfaceMuted else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(CStoriesRadii.sm),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
