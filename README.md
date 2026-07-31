# CStories

CStories is a Kotlin Multiplatform library that brings a Storybook or Widgetbook-style component catalog experience to Compose Multiplatform.

## Problem

Compose Multiplatform does not currently have a true Storybook or Widgetbook equivalent.

Showkase gets close, but it is Android-only and couples demo annotations directly to design system components. That makes it a poor fit for a multiplatform cataloging workflow where the design system should stay clean and independent from the preview tool.

## Goal

The goal of CStories is to provide a publishable Kotlin Multiplatform library that any Compose Multiplatform project can add as a regular dependency plus a KSP processor.

With it, teams can define stories as dedicated demo composables kept separate from the design system itself, then expose them in a web catalog powered by the `wasmJs` target.

The intended experience includes:

- hierarchical navigation through stories
- isolated rendering for each story
- interactive knobs for exploring component states and variations

## Core Principle

In CStories, `@CStory` is never placed on the design system component itself.

Instead, it is placed on a dedicated demonstration function. This keeps the design system free from any dependency on CStories, allows multiple stories to exist for the same component, and keeps the mental model simple: stories are just regular composables with regular Compose state.

This approach also avoids runtime reflection, which is important for Kotlin/Wasm compatibility.

## Architecture

The project is designed around five modules:

- `cstories-annotations`: a dependency-free Kotlin Multiplatform module containing the `@CStory` annotation with source retention only
- `cstories-processor`: a JVM-only KSP processor that discovers stories, validates them, generates per-module registries, and emits manifests for aggregation
- `cstories-runtime`: a Compose Multiplatform runtime module that provides the catalog application shell, navigation tree, story frame, and knob composables
- `cstories-gradle-plugin`: a Gradle plugin that wires the executable `wasmJs` target, adds the required dependencies, generates the catalog entry point, and aggregates registries across modules
- `sample`: a dogfooding module used to validate the end-to-end developer experience

## Story Organization

Stories are organized with a `group` and a `name`.

The group can be hierarchical, which allows catalogs such as `DesignSystem/Buttons`, while the name remains the final leaf displayed in the navigation tree. This makes it possible to present stories in a familiar structure such as `Button > Primary` or `Card > Elevated`.

The runtime is responsible for rebuilding that hierarchy into a navigation tree for the sidebar.

## Developer Experience

The intended developer experience is deliberately simple.

A consumer applies the CStories Gradle plugin to the module that contains stories. The plugin then configures the executable `wasmJs` target, adds the required dependencies, generates the application entry point, and makes the catalog launchable directly from the IDE.

The long-term goal is to make running the catalog feel like running any normal Compose Multiplatform web app, without forcing consumers to handcraft a separate preview application.

### Watch mode

`./gradlew runCStories` runs the catalog once. For active development, run it with Gradle's continuous build instead:

```
./gradlew runCStories --continuous
```

Gradle watches the project sources and automatically recompiles the `wasmJs` target whenever a story or component changes. The webpack dev server then reloads the page in the browser.

This is a full page reload, not a state-preserving hot reload: navigation state in the catalog (selected story, knob values, and so on) is lost on every reload, and the reload takes a few seconds depending on project size. True hot reload with state preservation is not currently available for the `wasmJs` target in the Kotlin/Compose Multiplatform ecosystem.

## Publishing Locally

CStories is not yet published to a remote repository — `mavenLocal()` is the only supported publication target for now.

Because `cstories-gradle-plugin` is a separate, included Gradle build (see `pluginManagement { includeBuild(...) }` in `settings.gradle.kts`), publishing everything requires two commands run from the repository root:

```
./gradlew publishToMavenLocal
./gradlew -p cstories-gradle-plugin publishToMavenLocal
```

The first command publishes `cstories-annotations`, `cstories-processor`, and `cstories-runtime`. The second publishes `cstories-gradle-plugin` itself, along with the plugin marker artifact needed to resolve `id("io.cstories.gradle")` from `mavenLocal()`.

The published group and version (`io.cstories` / `0.1.0-SNAPSHOT` by default) come from the root `gradle.properties` (`cstoriesGroup` / `cstoriesVersion`), which is the single source of truth — update it there if you need a different version, then republish with both commands above.

## Using CStories in an External Project

Once published to `mavenLocal()`, any other Compose Multiplatform project can depend on CStories without needing this repository at all.

**1. Add `mavenLocal()` to plugin resolution**, in the consumer project's `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

**2. Apply the plugin** on the module that contains (or will contain) stories, alongside Compose Multiplatform:

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("io.cstories.gradle") version "0.1.0-SNAPSHOT"
}
```

The plugin takes care of the rest: it adds the executable `wasmJs` target, wires `cstories-annotations`, `cstories-runtime`, and the `cstories-processor` KSP dependency, and generates the catalog's entry point. No manual dependency declarations for CStories itself are needed.

**3. Write a story**, kept separate from the design system component it demonstrates:

```kotlin
import io.cstories.annotations.CStory

@CStory(collection = "DesignSystem", group = "Button", name = "Primary")
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(text = "Click me", onClick = {})
}
```

**4. Run the catalog**:

```
./gradlew runCStories
```

**5. Produce a servable static site** (for hosting, sharing, or CI) with:

```
./gradlew wasmJsBrowserDistribution
```

The output lands in `build/dist/wasmJs/productionExecutable`. The catalog's Export button (in the running app) also builds a standalone, self-contained zip of the current site client-side and triggers a browser download of it, whether running via `runCStories` or from a production distribution.

## License

See [LICENSE](./LICENSE).
