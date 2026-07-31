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

## License

See [LICENSE](./LICENSE).
