---
title: Publish CStories locally
---

# Publish CStories locally

Released versions of CStories are published to GitHub Packages. If you're testing unreleased changes or a specific
local version, you'll need to (re)publish it to `mavenLocal()`.

## Publishing everything

From the CStories repository:

```bash
./gradlew publishAllToMavenLocal
```

Because `cstories-gradle-plugin` is a separate, included Gradle build, its own `publishToMavenLocal` task isn't
picked up by the root project's — this aggregate task wires both together. It publishes:

- `cstories-annotations`
- `cstories-processor`
- `cstories-runtime`
- `cstories-gradle-plugin`, along with the plugin marker artifact needed to resolve `id("dev.cstories.gradle")` from
  `mavenLocal()`

## Versioning

The published group (`dev.cstories`) and version come from the root `gradle.properties`
(`cstoriesGroup` / `cstoriesVersion`), which is the single source of truth.

## When to republish

Republish with `publishAllToMavenLocal` any time you:

- pull new changes from the CStories repository
- change the version in `gradle.properties` for a specific test
- notice your consumer project resolving a stale version from `mavenLocal()`
