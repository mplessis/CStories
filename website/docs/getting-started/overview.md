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

For a first setup, we recommend starting with a single module targeting `jvm()` only. It's the simplest path: no
browser tooling, no extra module split, just a Gradle plugin and a first story.

Continue to [Installation](/getting-started/installation) to set this up.
