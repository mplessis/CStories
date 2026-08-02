package io.cstories.annotations

/**
 * Marks a design-system function as a documentable component.
 *
 * Applying this annotation on a top-level function or an object/companion
 * object member function makes it available through the generated
 * `GeneratedComponentRefs` object, so a `@CStory` can safely reference it via
 * `component = GeneratedComponentRefs.Xxx` instead of a hand-typed string.
 *
 * The KDoc written on the annotated function is what gets surfaced in the
 * story catalog's documentation panel.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CStoryComponent
