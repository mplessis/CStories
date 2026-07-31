package io.cstories.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CStory(
    val group: String,
    val name: String,
    val tags: Array<String> = [],
)
