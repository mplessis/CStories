---
title: Constraints and limitations
---

# Constraints and limitations

## Distribution

CStories is currently published to `mavenLocal()` only. There is no remote repository yet, so every consumer project
needs `mavenLocal()` declared in both `pluginManagement` and `dependencyResolutionManagement`.

## Target requirements

Applying `io.cstories.gradle` requires at least one of `jvm()` or `wasmJs` to be declared on the module. Applying it
with neither fails fast with a clear error, instead of silently defaulting to one.

## `wasmJs` specifics

`wasmJs` requires `browser()` and `binaries.executable()` to be declared explicitly in your own `kotlin { }` block —
the plugin cannot safely reconfigure these after the target has been finalized. See
[Run the web catalog](/guides/web-catalog) for details.

The web catalog's watch mode (`--continuous`) triggers a full page reload on every change: navigation state
(selected story, knob values) is lost each time, and reloads take a few seconds depending on project size. There is
currently no state-preserving hot reload available for the `wasmJs` target.

## Theme wrapper uniqueness

Only one `@CStoryThemeWrapper` property is allowed across the whole project. Declaring more than one causes the
build to fail with a clear error.

## Multi-module component references

`@CStoryComponent` is only visible to KSP within the module it's declared in. When components and stories live in
different modules, the components module must also apply `io.cstories.gradle.components` for
`CStoryComponentRefs` to be generated. See
[Reference a component in a story](/guides/component-references).
