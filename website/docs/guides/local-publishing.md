---
title: Publish CStories locally
---

# Publish CStories locally

CStories is not yet published to a remote repository — `mavenLocal()` is the only supported publication target for
now. If you're testing changes or trying out a specific version, you'll need to (re)publish it locally.

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
- `cstories-gradle-plugin`, along with the plugin marker artifact needed to resolve `id("io.cstories.gradle")` from
  `mavenLocal()`

## Versioning

The published group and version (`io.cstories` / `0.1.0-SNAPSHOT` by default) come from the root `gradle.properties`
(`cstoriesGroup` / `cstoriesVersion`), which is the single source of truth.

## When to republish

Republish with `publishAllToMavenLocal` any time you:

- pull new changes from the CStories repository
- change the version in `gradle.properties` for a specific test
- notice your consumer project resolving a stale version from `mavenLocal()`
