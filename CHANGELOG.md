# Changelog

## 2.0.5

### Fixed

- `dev.cstories.gradle`: the plugin is now compatible with the Gradle configuration cache. The KSP option
  `cstories.componentMetadataHash` captured the `cstoriesExtractComponentMetadata` task and was empty when the cache
  entry was stored (`Processor arguments not in the format \S+=\S+`). It is removed: the component metadata file is now
  a declared input of the KSP tasks, which run non-incrementally when it changes, so KDoc-only edits in a components
  module are still reflected in `CStoryComponentRefs`.

## 2.0.4

### Changed

- Artifacts are now published to GitHub Packages (`https://maven.pkg.github.com/mplessis/CStories`) instead of Azure
  Artifacts, through a release workflow triggered by `v*` tags.

## 2.0.3

### Added

- `cstories-processor`: explicit KDoc links (`[Declaration]`) are resolved into related documentation entries.
- `cstories-runtime`: anchor links are supported in markdown documentation.

## 2.0.2

### Fixed

- `dev.cstories.gradle`: editing only the KDoc of a `@CStoryComponent` declared in a dependency module now
  regenerates `CStoryComponentRefs` (and its `@GeneratedComponentDocumentation`) in the stories module. Previously the
  change was invisible to the incremental KSP pass and required `--rerun-tasks`.
- `dev.cstories.gradle.components`: Compose Multiplatform `copy*ComposeResources` tasks now depend on the KSP task
  producing `META-INF/cstories/components.txt`. The consumer-side workaround added for 2.0.0 can be removed.
