---
title: Installation
---

# Installation

At this stage, CStories is published to `mavenLocal()` only.

## 1. Publish CStories locally

From the [CStories repository](https://github.com/mplessis/CStories), run:

```bash
./gradlew publishAllToMavenLocal
```

This command publishes the required artifacts (`cstories-annotations`, `cstories-processor`, `cstories-runtime`) as
well as the `cstories-gradle-plugin` itself to your local Maven repository.

## 2. Declare `mavenLocal()` in the consumer project

In the `settings.gradle.kts` of the project that will use CStories, add `mavenLocal()` to both repository blocks:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
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

These are two separate repository blocks: `pluginManagement` resolves the plugin itself, while
`dependencyResolutionManagement` resolves the actual `io.cstories:*` library artifacts. Missing either one causes
resolution errors.

## 3. Apply the CStories plugin

In the module that will host your stories:

```kotlin
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

For a first use, the recommended path is to start with the `jvm()` target only, in order to run the catalog as a
desktop application. Applying the plugin with no target declared fails fast with a clear error instead of silently
forcing one.

## 4. Run the catalog

```bash
./gradlew runCStoriesDesktop
```

If the module also declares a `wasmJs` target, the catalog can also be launched in the browser:

```bash
./gradlew runCStoriesWasm
```

Next: [Create your first story](/getting-started/first-story).
