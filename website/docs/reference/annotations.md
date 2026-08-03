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

Applies to: top-level `@Composable` functions.

## `@CStoryComponent`

Marks a design-system function as a documentable component, making it available through the generated
`CStoryComponentRefs` object so a `@CStory` can safely reference it, and surfacing its KDoc in the catalog's
documentation panel.

Applies to: top-level functions, or object/companion object member functions.

## `@CStoryThemeWrapper`

Marks a top-level property that supplies the whole catalog's theme wrapper, used to preview stories under your own
design-system theme instead of the runtime's default Material3 fallback.

Applies to: top-level properties only. At most one such property is allowed across the whole project.

See also: [Reference a component in a story](/guides/component-references) and
[Customize the catalog theme](/guides/theming).
