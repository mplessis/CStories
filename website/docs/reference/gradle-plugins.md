---
title: Gradle plugins
---

# Gradle plugins

## `dev.cstories.gradle`

The main catalog plugin. Applies to the module that hosts your stories.

- adds the required CStories dependencies (`cstories-annotations`, `cstories-runtime`, the `cstories-processor` KSP
  dependency)
- generates a catalog entry point for whichever target(s) (`jvm()`, `wasmJs`) the module already declares
- registers the `runCStoriesDesktop`, `runCStoriesDesktopHotReload`, `runCStoriesWasm`, and `cstoriesExportWeb` tasks

Applying it with neither `jvm()` nor `wasmJs` declared fails fast with a clear error.

## `dev.cstories.gradle.components`

A lightweight plugin for plain component/design-system library modules. Applies to the module that declares your
components, when it's different from the module that declares the stories demonstrating them.

- does not apply Compose Multiplatform
- does not require a `jvm()`/`wasmJs()` target
- does not wire any catalog/entry-point task
- makes `cstories-annotations` available at compile time without publishing it as a runtime dependency
- wires KSP to process `@CStoryComponent` and generate `io.cstories.generated.CStoryComponentRefs`

See [Reference a component in a story](/guides/component-references) for when this plugin is needed.

## Choosing between the two

| Situation | Plugin to apply |
|---|---|
| Components and stories live in the same module | `dev.cstories.gradle` only |
| Components and stories live in different modules | `dev.cstories.gradle` on the stories module, `dev.cstories.gradle.components` on the components module |
