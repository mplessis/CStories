---
title: Structure a multi-module project
---

# Structure a multi-module project

For a first project, keeping components and stories in the same module is perfectly fine. As your design system
grows, though, you may want to keep stories in a dedicated module instead.

## When a single module is enough

If your design system module already targets `jvm()` (or you're fine adding it), you can apply the CStories plugin
**directly on it**. A `jvm()`-only catalog needs no `wasmJs` target at all, so it never forces Gradle to resolve
`commonMain` dependencies for a platform your module doesn't otherwise support:

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

## When to split components and stories

If you also want (or only want) the web catalog, and your design system module already targets other platforms and
pulls in dependencies that aren't published for `wasmJs` (a private icon library, a platform-specific SDK, ...),
applying the plugin directly to that module with `wasmJs` declared forces Gradle to resolve *all* of its
`commonMain` dependencies for `wasmJs` too — the build can break with errors like:

```text
Could not resolve com.example:some-native-only-lib:1.0.0.
Required by:
    project :lib
```

The recommended fix mirrors CStories' own core principle (`@CStory` never lives on the design system component
itself) at the module level: keep a **separate stories module** that depends on `:lib` as a regular dependency, and
is the only place `wasmJs` and the CStories plugin get applied. `:lib` itself stays completely untouched — no new
target, no new dependency resolution constraints.

```text
:lib            // your design system, untouched — jvm, ios, android, whatever it already targets
:lib:stories    // new module — depends on :lib, applies the CStories plugin, targets wasmJs (and/or jvm)
```

## Setting it up

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

Stories live in `lib/stories/src/commonMain`, importing components from `:lib` and demonstrating them. `:lib` never
depends on CStories, and `:lib:stories` never needs to resolve `:lib`'s non-`wasmJs`-published dependencies for any
target other than `wasmJs`.

The plugin takes care of the rest for whichever target(s) you declared: it wires `cstories-annotations`,
`cstories-runtime`, and the `cstories-processor` KSP dependency, and generates the catalog's entry point(s).

## What's next

If your component and its story live in different modules, referencing the component safely from the story requires
one extra step — see [Reference a component in a story](/guides/component-references).
