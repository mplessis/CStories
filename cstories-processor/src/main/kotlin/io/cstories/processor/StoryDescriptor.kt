package io.cstories.processor

import com.google.devtools.ksp.symbol.KSFile

internal data class StoryDescriptor(
    val collection: String,
    val group: String,
    val name: String,
    val invoker: StoryInvoker,
    val originatingFile: KSFile?,
) {
    val pathSegments: List<String>
        get() = collection.split('/') + group.split('/') + name
}

internal sealed interface StoryInvoker {
    data class TopLevel(
        val packageName: String,
        val functionName: String,
    ) : StoryInvoker

    data class ObjectMember(
        val packageName: String,
        val objectName: String,
        val functionName: String,
    ) : StoryInvoker
}

internal data class GeneratedRegistry(
    val packageName: String,
    val objectName: String,
) {
    val qualifiedName: String
        get() = "$packageName.$objectName"
}
