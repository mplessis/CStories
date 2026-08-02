package io.cstories.annotations

/**
 * Marks a top-level property that supplies the whole catalog's
 * `io.cstories.runtime.CStoriesThemeWrapper`, wrapping every previewed story
 * composable in the consumer's own design-system theme (e.g. a
 * `LumenTheme(isDark) { ... }`) instead of the runtime's default plain
 * Material3 fallback.
 *
 * At most one property across the whole dependency graph may carry this
 * annotation — the generated catalog build fails with a clear error if more
 * than one is found.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class CStoryThemeWrapper
