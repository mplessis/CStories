## Project Rules

- Follow the architecture described in the Jotty notes for CStories.
- Keep the implementation minimal and incremental.
- Prefer small, focused changes over speculative abstractions.
- Do not add CI, release automation, or remote publishing unless explicitly requested.

## Commit Rules

- All commits must follow the Conventional Commits v1.0.0 specification.
- All commit messages must be written in English.
- Prefer scopes matching the affected module name, for example `feat(cstories-annotations): add CStory annotation`.
- Never add AI or agent attribution in commits.
- Never add `Co-Authored-By` footers for the agent.
- Never add tool signatures or footer mentions in commit messages.
- Do not create empty commits.
- Do not amend commits unless explicitly requested.

## Project Structure

- `cstories-annotations`: Kotlin Multiplatform module containing only source-retained annotations and no dependencies.
- `cstories-processor`: JVM-only KSP processor module generating story registries and manifests.
- `cstories-runtime`: Kotlin Multiplatform Compose runtime module for the catalog UI and knobs.
- `cstories-gradle-plugin`: Gradle plugin module wiring `wasmJs`, dependencies, generated entry point, and registry aggregation.
- `sample`: dogfooding module used to validate the end-to-end developer experience.

## Technical Constraints

- `cstories-annotations` must stay dependency-free, including no Compose dependency.
- `@CStory` must keep `AnnotationTarget.FUNCTION` and `AnnotationRetention.SOURCE`.
- Validation logic belongs in the KSP processor, not in the annotation module.
- `cstories-processor` must remain a JVM module.
- Generated story invocation must use direct compiled calls, never runtime reflection.
- Keep Kotlin/Wasm compatibility as a core constraint for all runtime and codegen decisions.

## Current Scope

- Current phase: scaffold the multi-module project structure.
- Publication target for now: `mavenLocal()` only.
- CI is out of scope until explicitly requested.
