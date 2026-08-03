---
title: Tasks and commands
---

# Tasks and commands

| Task | Purpose | When to use |
|---|---|---|
| `publishAllToMavenLocal` | Publishes every `io.cstories:*` artifact, including the Gradle plugin, to `mavenLocal()`. | Run from the CStories repository, before using it in another project. |
| `runCStoriesDesktop` | Runs the catalog as a desktop application (`jvm()` target). | Everyday local development. |
| `runCStoriesDesktopHotReload` | Runs the desktop catalog with hot reload. | Active iteration on stories, without restarting the app. |
| `runCStoriesWasm` | Runs the catalog in the browser (`wasmJs` target). Add `--continuous` for watch mode. | Validating the web target, or when your module only targets `wasmJs`. |
| `wasmJsBrowserDistribution` | Produces the static, production-ready `wasmJs` distribution. | Preparing the catalog for hosting. |
| `cstoriesExportWeb` | Packages the `wasmJs` distribution as a zip, ready to host anywhere. | Sharing or deploying a static build of the catalog. |
