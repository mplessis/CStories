---
title: Customize the catalog theme
---

# Customize the catalog theme

Every story's canvas has a light/dark switch in its toolbar, letting you check how a component looks against both
backgrounds without leaving the catalog.

## Default behavior

By default, toggling it wraps the previewed story in a plain Material3 `MaterialTheme` using
`darkColorScheme()`/`lightColorScheme()` — enough for components that already rely on `MaterialTheme.colorScheme`
for their colors.

## Using your own theme

If your design system uses its own theme instead of Material3 (a `LumenTheme(isDark) { ... }`, for example), point
the catalog at it by annotating a single top-level property with `@CStoryThemeWrapper`, anywhere in your project:

```kotlin
import io.cstories.annotations.CStoryThemeWrapper
import io.cstories.runtime.CStoriesThemeWrapper

@CStoryThemeWrapper
val LumenCStoriesThemeWrapper: CStoriesThemeWrapper = { isDark, content ->
    LumenTheme(isDark = isDark, content = content)
}
```

`cstories-processor` picks this up via KSP — no Gradle configuration needed. The generated entry point then wraps
every previewed story in `LumenTheme(isDark = ...)` instead of the Material3 default, so the actual rendered colors
(not just the canvas backdrop) reflect the toggle.

## Constraints

Only one `@CStoryThemeWrapper` property is allowed across the whole project. The build fails with a clear error if
more than one is found, and KSP reports an error if the annotation is applied to anything other than a top-level
property.
