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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.resources.Res
import io.cstories.runtime.resources.main_header_export_button
import io.cstories.runtime.resources.main_header_export_failed
import io.cstories.runtime.resources.main_header_export_unavailable_dev
import io.cstories.runtime.resources.main_header_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Top of the "Story Preview" pane: title, breadcrumb subtitle and the
 * export action, mirroring the mockup's main-header. Export builds a
 * standalone, self-contained copy of the catalog as a zip, entirely
 * client-side, and triggers a browser download of it — see
 * [triggerStandaloneExport].
 */
@Composable
fun MainHeader(breadcrumbPath: List<String>, modifier: Modifier = Modifier) {
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exportUnavailableMessage = stringResource(Res.string.main_header_export_unavailable_dev)
    val exportFailedMessage = stringResource(Res.string.main_header_export_failed)

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
            exportMessage?.let { message ->
                Text(
                    text = message,
                    color = CStoriesColors.text,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Button(
            onClick = {
                scope.launch {
                    when (val result = triggerStandaloneExport()) {
                        is StandaloneExportResult.Success -> Unit
                        is StandaloneExportResult.NotAvailable -> exportMessage = exportUnavailableMessage
                        is StandaloneExportResult.Failure -> exportMessage = exportFailedMessage.replace("%1\$s", result.message)
                    }
                    if (exportMessage != null) {
                        delay(4000)
                        exportMessage = null
                    }
                }
            },
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
                text = stringResource(Res.string.main_header_export_button),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
