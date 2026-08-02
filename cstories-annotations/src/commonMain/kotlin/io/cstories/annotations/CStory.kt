package io.cstories.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CStory(
    val collection: String,
    val group: String,
    val name: String,
    val tags: Array<String> = [],
    val component: String = "",
)
