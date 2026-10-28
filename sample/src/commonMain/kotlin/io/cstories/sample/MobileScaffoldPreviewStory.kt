package io.cstories.sample

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.cstories.annotations.CStory
import io.cstories.runtime.MobileDevicePreview

@CStory(collection = "Runtime", group = "Previews", name = "Mobile scaffold")
@Composable
fun MobileScaffoldPreviewStory() {
    MobileDevicePreview(contentPadding = PaddingValues(0.dp)) {
        Scaffold(
            topBar = { DemoTopBar(title = "Activities", onBack = {}) },
            bottomBar = {
                DemoButton(
                    text = "Continue",
                    onClick = {},
                    size = ButtonSize.Large,
                    fullWidth = true,
                    modifier = Modifier.padding(16.dp),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                itemsIndexed(sampleMembers + sampleMembers) { index, (memberName, _) ->
                    DemoListItem(
                        title = "Activity ${index + 1}",
                        subtitle = "Completed today by $memberName",
                        leading = { DemoAvatar(name = memberName, size = AvatarSize.Small) },
                    )
                }
            }
        }
    }
}
