package io.cstories.runtime.knobs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * "Portal" used to move a story's [KnobPanel] content out of the canvas
 * stage and into the runtime's separate controls panel, without requiring
 * story authors to change how they write stories (knobs are still declared
 * inline via [KnobPanel] inside the story composable).
 */
internal val LocalControlsSlot: ProvidableCompositionLocal<MutableState<(@Composable () -> Unit)?>?> =
    staticCompositionLocalOf { null }

/**
 * "Portal" used to publish the current values of a story's knobs (keyed by
 * the `codeKey` each knob declares) out to the runtime, so the "Code" tab
 * (see [io.cstories.runtime.DocsCodeTabs]) can substitute them into the
 * story's usage snippet and stay in sync with the live preview.
 */
internal val LocalKnobValuesSlot: ProvidableCompositionLocal<MutableState<Map<String, String>>?> =
    staticCompositionLocalOf { null }
