package io.cstories.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CStory(
    val collection: String,
    val group: String,
    val name: String,
    val tags: Array<String> = [],
    val component: String = "",
    val themeWrapper: KClass<*> = Any::class,
)
