# CStories

<p align="center">
  <img src="docs/assets/logo-inline.png" alt="CStories" width="400" />
</p>
<br/>

**CStories** is a component cataloging solution for Compose Multiplatform.

Designed for teams building and maintaining a design system, CStories lets you centralize UI demonstrations in a
dedicated catalog, inspired by the workflow popularized by Storybook, while respecting the constraints specific to the
Kotlin Multiplatform ecosystem.

The approach behind CStories is simple: components stay independent, and demonstrations are written in dedicated
stories. The catalog is then automatically generated from these stories and can run as a desktop app (`jvm`) or in the
browser (`wasmJs`), depending on the targets declared by the consumer module.

## What the project does

CStories gives Compose Multiplatform developers a simple framework to:

- build a navigable catalog of components
- isolate components in dedicated stories
- organize demonstrations by collection, group, and name
- speed up iteration on UI components
- lay a clean foundation for the visual documentation of a design system

The core principle of the solution is the following:

- design system components are never annotated directly as stories
- each story is a composable dedicated to demonstrating a component
- that story remains a plain Compose function, annotated with `@CStory`

This separation keeps the design system readable, allows multiple demonstrations for the same component, and stays
compatible with Kotlin/Wasm.

## Installation

At this stage, CStories is published to `mavenLocal()` only.

### 1. Publish CStories locally

From this repository, run:

```bash
./gradlew publishAllToMavenLocal
```

This command publishes the required artifacts as well as the Gradle plugin to your local Maven repository.

### 2. Declare `mavenLocal()` in the consumer project

In the `settings.gradle.kts` of the project that will use CStories:

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

This configuration is required both to resolve the plugin and to resolve the dependencies published by CStories.

### 3. Apply the CStories plugin

In the module that will host the stories:

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

For a first use, the recommended path is to start with the `jvm()` target, in order to run the catalog as a desktop
application.

### 4. Run the catalog

```bash
./gradlew runCStoriesDesktop
```

If the module also declares a `wasmJs` target, the catalog can also be launched in the browser:

```bash
./gradlew runCStoriesWasm
```

## Creating your first story

A story is a composable dedicated to demonstrating a component in the catalog.

### 1. Define a Compose component

Example:

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text)
    }
}
```

### 2. Create the associated story

Add a separate composable function, annotated with `@CStory`:

```kotlin
import androidx.compose.runtime.Composable
import io.cstories.annotations.CStory

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Primary"
)
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(
        text = "Hello CStories",
        onClick = {},
    )
}
```

### 3. Structure the catalog display

The `@CStory` parameters determine the navigation structure:

- `collection`: root level of the catalog
- `group`: logical grouping of stories
- `name`: story label displayed in the interface

With this example, the story will appear in a structure such as:

```text
DesignSystem / Buttons / Primary
```

### 4. Run and verify

Launch the catalog:

```bash
./gradlew runCStoriesDesktop
```

The story should then appear in the navigation and let you view the component in an isolated context.

## Advanced documentation

More advanced use cases will be covered in dedicated documentation, notably for:

- multi-module architectures
- advanced usage of `wasmJs`
- component reference generation
- component-associated documentation
- catalog theme customization
- exporting the catalog for the web
