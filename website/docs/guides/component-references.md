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
fun PrimaryButton(text: String, onClick: () -> Unit) { /* ... */
}
```

`@CStoryComponent` can be applied to a top-level function or an object/companion object member function.

By default, generated references stay at the root of `CStoryComponentRefs`.
Only components that need an explicit version should opt into a namespace.

```kotlin
@CStoryComponent
@Composable
fun Primary() { /* ... */
}

// Generates CStoryComponentRefs.Button.Primary
```

When two components share the same simple reference path but come from different packages, you can opt one of them
into a namespace:

```kotlin
object LumenIconOnBackground {
    @CStoryComponent(namespace = "v2")
    @Composable
    fun Brand() { /* ... */
    }

    @CStoryComponent(namespace = "v2")
    @Composable
    fun Warning() { /* ... */
    }
}

// Generates CStoryComponentRefs.v2.LumenIconOnBackground.Brand
// and       CStoryComponentRefs.v2.LumenIconOnBackground.Warning
```

The namespace must be a single valid Kotlin identifier. CStories does not derive it from the package name.

## Applying the components plugin

This requires applying an additional, lightweight plugin **directly on the module that declares the component**
(`:lib`, not `:lib:stories`): `id("dev.cstories.gradle.components")`. Unlike `id("dev.cstories.gradle")` (the catalog
plugin), this one doesn't apply Compose Multiplatform, doesn't require a `jvm()`/`wasmJs()` target, and doesn't wire
any catalog/entry-point task — it only wires KSP to process `@CStoryComponent` and generate
component metadata. The stories module then generates `io.cstories.generated.CStoryComponentRefs` locally, so the
generated refs object is not part of the published component API:

```kotlin
// lib/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("dev.cstories.gradle.components") version "1.1.5"
}
```

This is required whenever the component and the story that demonstrates it live in **different Gradle modules**
(the `:lib` / `:lib:stories` split — see [Structure a multi-module project](/guides/multi-module-setup)): KSP only
ever scans annotated symbols within the module it's currently processing, never across a dependency boundary.
Applying `dev.cstories.gradle.components` directly on `:lib` publishes only internal CStories component metadata. The
stories module consumes that metadata and generates `CStoryComponentRefs` in its own compilation, while the KDoc is
copied into the generated `StoryEntry` used by the runtime documentation tab.

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

For a namespaced component, reference the nested object explicitly:

```kotlin
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs

@CStory(
    collection = "v2/Components",
    group = "Content Display/LumenIconOnBackground",
    name = "Brand",
    component = CStoryComponentRefs.v2.LumenIconOnBackground.Brand,
)
@Composable
fun LumenIconOnBackgroundBrandV2Story() {
    LumenIconOnBackground.Brand()
}
```

## When this isn't needed

If your component and its story live in the **same module**, the catalog plugin can process both directly. This setup
is only recommended for application-owned or non-published modules.

For a reusable published library, keep the story in a separate module. Apply `dev.cstories.gradle.components` to the
component module and `dev.cstories.gradle` to the stories module so that `cstories-runtime` does not become a
dependency of library consumers.

## What's next

Once a component is annotated, its KDoc can also power the catalog's documentation panel — see
[Document a component](/guides/component-documentation).
