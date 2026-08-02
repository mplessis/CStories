package io.cstories.annotations

/**
 * Carries a `@CStoryComponent` function's KDoc, already rendered to
 * Markdown, on the matching generated `CStoryComponentRefs` property.
 *
 * KSP only exposes `docString` for symbols declared in the module it is
 * currently compiling — KDoc comments are source-only and stripped from
 * `.class` files, so a `@CStory` living in a *different* module than the
 * `@CStoryComponent` it references can never read that KDoc directly.
 * Annotations, on the other hand, are retained in `.class`/klib metadata and
 * do survive across that same module boundary — `cstories-processor` bakes
 * the pre-rendered Markdown into this annotation while it still has access
 * to the source (i.e. while processing the component's own module), so a
 * `@CStory` in any downstream module can read it back from the generated
 * refs instead.
 *
 * Generated only, never meant to be applied manually.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
public annotation class GeneratedComponentDocumentation(val markdown: String)
