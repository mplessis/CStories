---
title: Overview
---

# Overview

CStories introduces a simple mental model. Before installing anything, it helps to understand what each piece does.

## What is a story?

A story is a regular `@Composable` function, annotated with `@CStory`, whose only job is to demonstrate a component
of your design system. It is never the component itself — it's a dedicated demonstration that calls into it.

```kotlin
@CStory(collection = "DesignSystem", group = "Buttons", name = "Primary")
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(text = "Click me", onClick = {})
}
```

## What does the Gradle plugin do?

Applying the CStories Gradle plugin to a module:

- adds the required CStories dependencies (`cstories-annotations`, `cstories-runtime`, and the `cstories-processor`
  KSP dependency)
- generates a catalog application entry point for whichever target(s) the module declares
- registers ready-to-use Gradle tasks to run and export that catalog

No separate preview application needs to be handcrafted — the catalog is generated directly from your stories.

## What gets generated?

Behind the scenes, a KSP processor scans your module for `@CStory`-annotated functions, validates them, and
generates a registry used by the catalog's navigation. This registry powers the catalog UI: hierarchical navigation,
isolated rendering per story, and interactive knobs for exploring component states.

## Desktop or web?

The catalog can run in two ways, depending on the Kotlin target(s) already declared by your module:

- **`jvm()`** — runs as a desktop application, no browser or Wasm toolchain required
- **`wasmJs { browser(); binaries.executable() }`** — runs in the browser via Kotlin/Wasm

Both can be declared side by side; use whichever fits your workflow.

## Recommended path to get started

For an application-owned design system or a non-published prototype, stories can live in the same module as the
components.

For a reusable or published component library, stories should live in a separate module. The catalog plugin adds
`cstories-runtime` and the catalog infrastructure to the module where it is applied. Applying it to the published
component library can therefore expose CStories runtime dependencies to every consumer, even when they only use the
components.

The recommended structure for a published library is:

```text
:lib          // reusable components, published to consumers
:lib:stories  // stories and catalog, not used by library consumers
```

Continue to [Structure a multi-module project](/guides/multi-module-setup) for the setup details, then follow
[Installation](/getting-started/installation) to configure CStories.

Continue to [Installation](/getting-started/installation) to set this up.
