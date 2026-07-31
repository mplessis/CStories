package io.cstories.runtime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top of the "Story Preview" pane: title, breadcrumb subtitle and the
 * (decorative) export action, mirroring the mockup's main-header.
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
                text = "Story Preview",
                color = CStoriesColors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (breadcrumbPath.isNotEmpty()) {
                Breadcrumb(path = breadcrumbPath, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = CStoriesColors.dark),
            shape = RoundedCornerShape(999.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.FileDownload,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Export",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
