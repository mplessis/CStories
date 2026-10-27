---
title: Constraints and limitations
---

# Constraints and limitations

## Distribution

CStories is published to GitHub Packages (see [Installation](/getting-started/installation)), which requires a GitHub
token with the `read:packages` scope, even for public packages. The repository must be declared in both
`pluginManagement` and `dependencyResolutionManagement`.

## Target requirements

Applying `dev.cstories.gradle` requires at least one of `jvm()` or `wasmJs` to be declared on the module. Applying it
with neither fails fast with a clear error, instead of silently defaulting to one.

## `wasmJs` specifics

`wasmJs` requires `browser()` and `binaries.executable()` to be declared explicitly in your own `kotlin { }` block —
the plugin cannot safely reconfigure these after the target has been finalized. See
[Run the web catalog](/guides/web-catalog) for details.

The web catalog's watch mode (`--continuous`) triggers a full page reload on every change: navigation state
(selected story, knob values) is lost each time, and reloads take a few seconds depending on project size. There is
currently no state-preserving hot reload available for the `wasmJs` target.

The copy buttons of the web catalog (Code tab, code examples in the Docs tab, watch command) use the browser clipboard,
which is only available in a secure context: the catalog must be served over HTTPS or from `localhost`. Otherwise,
these buttons have no effect.

## Home page

The home page introduction is read from a file named exactly `CSTORIES.md`, at the root of the module that applies
`dev.cstories.gradle`. Its name and location can't be configured, and a `CSTORIES.md` file in another module (for
example a components module) is ignored. See [Home page](/guides/home-page).

## Theme wrappers

The global theme wrapper is an accessible Kotlin `object` named `CustomCStoriesThemeWrapper` implementing
`CStoriesThemeWrapper`. It is discovered automatically. Each story may override it with
`@CStory(themeWrapper = WrapperObject::class)`. Only one global wrapper may exist across the aggregated project.

## Multi-module component references

`@CStoryComponent` is only visible to KSP within the module it's declared in. When components and stories live in
different modules, the components module must also apply `dev.cstories.gradle.components` for
`CStoryComponentRefs` to be generated. See
[Reference a component in a story](/guides/component-references).

## Published library modules

A module that is published as a reusable component library must not also host the CStories catalog.

Applying `dev.cstories.gradle` adds `cstories-runtime` and catalog dependencies to that module. Because Gradle
publishes the module's dependency metadata, consumers may be required to resolve CStories artifacts even if they only
use the components.

Use two modules instead:

```text
:design-system          // published component library
:design-system:stories  // catalog and stories, not consumed by library users
```

The component module may use `dev.cstories.gradle.components`, which keeps `cstories-annotations` compile-only. The
stories module applies `dev.cstories.gradle` and owns the CStories runtime.
