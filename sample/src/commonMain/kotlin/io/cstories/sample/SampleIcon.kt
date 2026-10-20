package io.cstories.sample

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Small curated icon set used by the sample stories, so a `SelectKnob`
 * can offer icons as plain enum entries.
 *
 * Components accept a regular [ImageVector]; stories pass `icon.vector`.
 */
enum class SampleIcon(val vector: ImageVector?) {
    None(null),
    Add(Icons.Filled.Add),
    Send(Icons.AutoMirrored.Filled.Send),
    Delete(Icons.Filled.Delete),
    Favorite(Icons.Filled.Favorite),
    Search(Icons.Filled.Search),
    Settings(Icons.Filled.Settings),
    Notifications(Icons.Filled.Notifications),
    Person(Icons.Filled.Person),
    Inbox(Icons.Filled.Inbox),
}
