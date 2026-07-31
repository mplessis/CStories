package io.cstories.runtime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.main_header_title
import org.jetbrains.compose.resources.stringResource

/**
 * Top of the "Story Preview" pane: title and breadcrumb subtitle, mirroring
 * the mockup's main-header.
 */
@Composable
fun MainHeader(breadcrumbPath: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.main_header_title),
                color = CStoriesColors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (breadcrumbPath.isNotEmpty()) {
                Breadcrumb(path = breadcrumbPath, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
