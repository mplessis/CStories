# Changelog

## 2.1.1

### Fixed

- `MobileDevicePreview` / `DevicePreview`: leaving a mobile preview while the built-in device catalog was loading no
  longer crashes the wasm catalog with `MissingResourceException … mobile-devices.json`. The catalog is now loaded once
  per session, and a failure to read it falls back to the project's additional devices instead of crashing.

## 2.1.0

### Changed

- `MobileDevicePreview`, `DesktopDevicePreview` and `DevicePreview` no longer scroll by default: the content receives
  the finite height of the simulated screen, like on a real device. Full-height content (`Scaffold`, `fillMaxSize`,
  `LazyColumn`, `Modifier.weight`…) no longer crashes with `Size(… x 2147483647) is out of range`. Pass
  `scrollable = true` to restore the previous scrolling behaviour for content taller than the screen.
- `MobileDevicePreview` accepts a `contentPadding` parameter replacing the hard-coded padding, e.g.
  `PaddingValues(0.dp)` for a full-screen `Scaffold`.
- Binary-incompatible change: the new parameters change the signatures of these composables. Libraries compiled
  against a previous version must be recompiled.

## 2.0.6

### Fixed

- `dev.cstories.gradle` / `dev.cstories.gradle.components`: `CStoryComponentRefs` is generated again for components
  declared in a separate multi-target module (e.g. Android, iOS, JS, wasmJs) and consumed by a wasmJs-only stories
  module. `META-INF/cstories/components.txt` was only packaged in processed resources, never in the jar or klib the
  stories module resolves. The components plugin now exposes it as a dedicated Gradle variant
  (`cstoriesComponentMetadataElements`), resolved by the stories plugin. Reading the jars is kept as a fallback for
  published dependencies.
- `dev.cstories.gradle`: a single-target (e.g. wasmJs-only) stories module no longer fails with
  `can be a part of only one module` when KSP adds its output directory after the plugin moved it to `commonMain`.
- `dev.cstories.gradle`: `cstoriesExtractComponentMetadata` logs a warning when a dependency applying
  `dev.cstories.gradle.components` provides no component metadata, instead of silently writing an empty file. The
  last component of the extracted metadata no longer loses an empty documentation field.

### Documentation

- Documented how the property paths in `CStoryComponentRefs` are built, including for companion object members.

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
