---
title: Reference a component in a story
---

# Reference a component in a story

By default, a story just calls a component directly, like any other composable. But when you want a safe reference
to it — used by `@CStory(component = ...)` to power the catalog's documentation panel — CStories provides
`@CStoryComponent`.

## Annotate your component

```kotlin
// lib/src/commonMain/kotlin/.../PrimaryButton.kt
import io.cstories.annotations.CStoryComponent

/**
 * High-emphasis filled button, used for the main call-to-action.
 *
 * @param text Label displayed inside the button.
 * @param onClick Called when the button is clicked.
 */
@CStoryComponent
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) { /* ... */ }
```

`@CStoryComponent` can be applied to a top-level function or an object/companion object member function.

## Applying the components plugin

This requires applying an additional, lightweight plugin **directly on the module that declares the component**
(`:lib`, not `:lib:stories`): `id("io.cstories.gradle.components")`. Unlike `id("io.cstories.gradle")` (the catalog
plugin), this one doesn't apply Compose Multiplatform, doesn't require a `jvm()`/`wasmJs()` target, and doesn't wire
any catalog/entry-point task — it only wires KSP to process `@CStoryComponent` and generate
`io.cstories.generated.CStoryComponentRefs`, an object exposing one FQN constant per annotated function:

```kotlin
// lib/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("io.cstories.gradle.components") version "0.1.0-SNAPSHOT"
}
```

This is required whenever the component and the story that demonstrates it live in **different Gradle modules**
(the `:lib` / `:lib:stories` split — see [Structure a multi-module project](/guides/multi-module-setup)): KSP only
ever scans annotated symbols within the module it's currently processing, never across a dependency boundary.
Applying `io.cstories.gradle.components` directly on `:lib` generates `CStoryComponentRefs` locally, in the same
compilation where the component's KDoc is still visible as source.

## Referencing the component from a story

```kotlin
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs

@CStory(collection = "DesignSystem", group = "Button", name = "Primary", component = CStoryComponentRefs.PrimaryButton)
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(text = "Click me", onClick = {})
}
```

## When this isn't needed

If your component and its story live in the **same module** (a single-module setup, applying only
`io.cstories.gradle`), `@CStoryComponent` and `id("io.cstories.gradle.components")` aren't required at all —
`component = "..."` can be omitted, or `@CStoryComponent` can still be used purely for the safe-reference benefit,
without the extra plugin (the catalog plugin already wires the same KSP processing for components declared in its
own module).

## What's next

Once a component is annotated, its KDoc can also power the catalog's documentation panel — see
[Document a component](/guides/component-documentation).
