# CStories

CStories is a Kotlin Multiplatform library that brings a Storybook or Widgetbook-style component catalog experience to Compose Multiplatform.

## Problem

Compose Multiplatform does not currently have a true Storybook or Widgetbook equivalent.

Showkase gets close, but it is Android-only and couples demo annotations directly to design system components. That makes it a poor fit for a multiplatform cataloging workflow where the design system should stay clean and independent from the preview tool.

## Goal

The goal of CStories is to provide a publishable Kotlin Multiplatform library that any Compose Multiplatform project can add as a regular dependency plus a KSP processor.

With it, teams can define stories as dedicated demo composables kept separate from the design system itself, then expose them in a catalog running either as a desktop application (`jvm` target) or as a web app (`wasmJs` target) — whichever platform(s) the consumer's module already targets.

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
- `cstories-gradle-plugin`: a Gradle plugin that auto-detects the `jvm()`/`wasmJs()` targets already declared by the consumer, adds the required dependencies, generates the corresponding catalog entry point(s), and aggregates registries across modules
- `sample`: a dogfooding module used to validate the end-to-end developer experience

## Story Organization

Stories are organized with a `group` and a `name`.

The group can be hierarchical, which allows catalogs such as `DesignSystem/Buttons`, while the name remains the final leaf displayed in the navigation tree. This makes it possible to present stories in a familiar structure such as `Button > Primary` or `Card > Elevated`.

The runtime is responsible for rebuilding that hierarchy into a navigation tree for the sidebar.

## Developer Experience

The intended developer experience is deliberately simple.

A consumer applies the CStories Gradle plugin to the module that contains stories, having already declared the Kotlin Multiplatform target(s) it wants for the catalog:

- `jvm()` — the plugin generates a Compose Desktop entry point, runnable via `./gradlew jvmRun` or the `runCStoriesDesktop` alias. No browser, no wasm toolchain required.
- `wasmJs { browser(); binaries.executable() }` — the plugin generates a Kotlin/Wasm browser entry point, runnable via `./gradlew runCStoriesWasm`.
- Both — both entry points are generated side by side; use whichever alias fits your workflow.

Applying the plugin with neither target declared fails fast with a clear error instead of silently forcing one.

In every case, the plugin adds the required CStories dependencies (`cstories-annotations`, `cstories-runtime`, the `cstories-processor` KSP dependency), generates the application entry point(s), and makes the catalog launchable directly from the IDE — no separate preview application to handcraft by hand.

> **Why does `wasmJs` need `browser()`/`binaries.executable()` declared explicitly, but not `jvm()`?** Kotlin finalizes a wasmJs target's binary/output-file conventions as soon as the target is configured. Reconfiguring `browser()`/`binaries.executable()` on it later (which is what the plugin would need to do, since it only detects declared targets after the consumer's own `kotlin { }` block has run) breaks those conventions. Declaring it yourself — standard practice for any Kotlin/Wasm app — avoids the issue entirely. `jvm()` has no equivalent finalization step, so a bare `jvm()` is enough.

### Watch mode

`./gradlew runCStoriesWasm` runs the web catalog once. For active development, run it with Gradle's continuous build instead:

```
./gradlew runCStoriesWasm --continuous
```

Gradle watches the project sources and automatically recompiles the `wasmJs` target whenever a story or component changes. The webpack dev server then reloads the page in the browser.

This is a full page reload, not a state-preserving hot reload: navigation state in the catalog (selected story, knob values, and so on) is lost on every reload, and the reload takes a few seconds depending on project size. True hot reload with state preservation is not currently available for the `wasmJs` target in the Kotlin/Compose Multiplatform ecosystem.

The desktop catalog (`./gradlew runCStoriesDesktop`) does not support continuous/watch mode — restart it manually after changes.

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

**1. Add `mavenLocal()` to both plugin and dependency resolution**, in the consumer project's `settings.gradle.kts`. These are two separate repository blocks — `pluginManagement` only resolves the plugin itself, while `dependencyResolutionManagement` resolves the actual `io.cstories:*` library artifacts (`cstories-annotations`, `cstories-runtime`); missing the second one causes errors like `Could not find io.cstories:cstories-annotations:0.1.0-SNAPSHOT`.

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
```

**2. Apply the plugin, declaring the target(s) you want the catalog to run on.**

If your design system module already targets `jvm()` (or you're fine adding it), you can apply the plugin **directly on it** — a `jvm()`-only catalog needs no `wasmJs` target at all, so it never forces Gradle to resolve `commonMain` dependencies for a platform your module doesn't otherwise support:

```kotlin
// lib/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("io.cstories.gradle") version "0.1.0-SNAPSHOT"
}

kotlin {
    jvm()
}
```

If you also want (or only want) the web catalog, and your design system module already targets other platforms and pulls in dependencies that aren't published for `wasmJs` (a private icon library, a platform-specific SDK, ...), applying the plugin directly to that module with `wasmJs` declared forces Gradle to resolve *all* of its `commonMain` dependencies for `wasmJs` too, and the build breaks with errors like:

```
Could not resolve com.example:some-native-only-lib:1.0.0.
Required by:
    project :lib
```

This mirrors CStories' own core principle (`@CStory` never lives on the design system component itself) at the module level: keep a **separate stories module** that depends on `:lib` as a regular dependency and is the only place `wasmJs` and the CStories plugin get applied. `:lib` itself stays completely untouched — no new target, no new dependency resolution constraints.

```
:lib            // your design system, untouched — jvm, ios, android, whatever it already targets
:lib:stories    // new module — depends on :lib, applies the CStories plugin, targets wasmJs (and/or jvm)
```

`settings.gradle.kts`:

```kotlin
include(":lib", ":lib:stories")
```

`lib/stories/build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("io.cstories.gradle") version "0.1.0-SNAPSHOT"
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm() // optional — add it for a desktop catalog alongside the web one
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib"))
        }
    }
}
```

Stories live in `lib/stories/src/commonMain`, importing components from `:lib` and demonstrating them — `:lib` never depends on CStories, and `:lib:stories` never needs to resolve `:lib`'s non-`wasmJs`-published dependencies for any target other than `wasmJs`.

The plugin takes care of the rest for whichever target(s) you declared: it wires `cstories-annotations`, `cstories-runtime`, and the `cstories-processor` KSP dependency, and generates the catalog's entry point(s). No manual dependency declarations for CStories itself are needed.

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
./gradlew runCStoriesDesktop   # jvm target — opens a desktop window
./gradlew runCStoriesWasm      # wasmJs target — opens a browser dev server
```

**5. Produce a servable static site** (for hosting, sharing, or CI) with:

```
./gradlew wasmJsBrowserDistribution
```

The output lands in `build/dist/wasmJs/productionExecutable`. To package it as a zip ready to host anywhere (S3, GitHub Pages, an internal file server, ...), run:

```
./gradlew cstoriesExportWeb
```

The zip is written to `build/cstories/<project-name>-web.zip`.

## License

See [LICENSE](./LICENSE).
