package io.cstories.processor

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/** A function annotated with `@CStoryComponent`, resolved and ready to be exposed as a safe reference. */
internal data class ComponentDescriptor(
    val namespace: String,
    val enclosingObjectName: String?,
    val functionName: String,
    val fqn: String,
    val function: KSFunctionDeclaration?,
    val originatingFile: KSFile?,
    val documentation: String? = null,
) {
    /** Unique key identifying this component within the generated refs object, used for collision detection. */
    val refKey: String
        get() = refPathSegments.joinToString(".")

    val refPathSegments: List<String>
        get() = buildList {
            namespace.takeIf(String::isNotEmpty)?.let(::add)
            enclosingObjectName?.let(::add)
            add(functionName)
        }
}
