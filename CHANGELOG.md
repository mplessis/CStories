# Changelog

## 2.0.2

### Fixed

- `dev.cstories.gradle`: editing only the KDoc of a `@CStoryComponent` declared in a dependency module now
  regenerates `CStoryComponentRefs` (and its `@GeneratedComponentDocumentation`) in the stories module. Previously the
  change was invisible to the incremental KSP pass and required `--rerun-tasks`.
- `dev.cstories.gradle.components`: Compose Multiplatform `copy*ComposeResources` tasks now depend on the KSP task
  producing `META-INF/cstories/components.txt`. The consumer-side workaround added for 2.0.0 can be removed.
