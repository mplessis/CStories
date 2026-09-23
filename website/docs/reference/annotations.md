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

Applies to: top-level `@Composable` functions. `Any::class` means that the catalog-level wrapper is used.

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
