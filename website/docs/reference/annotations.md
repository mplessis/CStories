---
title: Annotations
---

# Annotations

## `@CStory`

Marks a composable function as a story, demonstrating a component in the catalog.

| Parameter | Type | Required | Description |
|---|---|---|---|
| `collection` | `String` | yes | Root level of the catalog navigation. |
| `group` | `String` | yes | Logical grouping of stories under a collection. Can be hierarchical. |
| `name` | `String` | yes | Story label displayed in the navigation. Must not contain `/`. |
| `tags` | `Array<String>` | no | Optional tags for the story. |
| `component` | `String` | no | Safe FQN reference to a `@CStoryComponent`, usually via `CStoryComponentRefs`. |
| `themeWrapper` | `KClass<*>` | no | An object implementing `CStoriesThemeWrapper` used only for this story. |
| `canvasScroll` | `CanvasScroll` | no | `Auto` (default) scrolls the canvas and device previews when the content is larger than them. `None` keeps the content within the preview area. |

Applies to: top-level `@Composable` functions. `Any::class` means that the catalog-level wrapper is used.

### Canvas scrolling

With `canvasScroll = CanvasScroll.Auto`, content larger than the canvas scrolls vertically and horizontally, and
content taller than a `MobileDevicePreview` or `DesktopDevicePreview` screen scrolls vertically, with scrollbars and
mouse wheel / trackpad support. Stories don't need to add `Modifier.verticalScroll(...)` themselves.

Smaller content keeps its layout: alignment, `fillMaxSize` and the background style are unchanged. Content always
receives finite constraints: content that cannot report its intrinsic size (`LazyColumn`, `Scaffold`,
`BoxWithConstraints` or any `SubcomposeLayout`) receives the size of the preview area, as before, and handles its own
scrolling.

The scroll position resets when another story is selected, and is kept while changing knobs or resetting them.

```kotlin
@CStory(collection = "Foundations", group = "Layout", name = "Pinned", canvasScroll = CanvasScroll.None)
@Composable
fun PinnedLayoutStory() { /* ... */ }
```

## `@CStoryComponent`

Marks a design-system function as a documentable component, making it available through the generated
`CStoryComponentRefs` object so a `@CStory` can safely reference it, and surfacing its KDoc in the catalog's
documentation panel.

| Parameter | Type | Required | Description |
|---|---|---|---|
| `namespace` | `String` | no | Optional Kotlin-identifier namespace used to nest the generated reference under `CStoryComponentRefs.<namespace>...`. Leave it blank to preserve the historical root-level reference. |

Applies to: top-level functions, or object/companion object member functions.

See also: [Reference a component in a story](/guides/component-references) and
[Customize the catalog theme](/guides/theming).
