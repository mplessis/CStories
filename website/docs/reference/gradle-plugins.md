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

### Global theme wrapper

The plugin automatically discovers an accessible object named `CustomCStoriesThemeWrapper` that implements
`CStoriesThemeWrapper`. No Gradle configuration is required. If it is not present, the runtime uses
`DefaultCStoriesThemeWrapper`.

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
| Published reusable library | `dev.cstories.gradle.components` on the component module, `dev.cstories.gradle` on a separate stories module |
| Application-owned design system | `dev.cstories.gradle` may be applied to the single module |
| Components and stories live in different modules | `dev.cstories.gradle` on the stories module, `dev.cstories.gradle.components` on the components module |

## Publication rule

Do not apply `dev.cstories.gradle` to a reusable component library that is published for external consumers.

That plugin adds `cstories-runtime` and catalog-related dependencies to the module where it is applied. Those
dependencies can therefore become part of the published dependency graph.

For a published library:

- the component module contains the reusable components;
- the component module may apply `dev.cstories.gradle.components`;
- a separate stories module depends on the component module;
- only the stories module applies `dev.cstories.gradle`.
