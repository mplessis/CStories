package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Generates `CStoryComponentRefs`, an object exposing a `const val` FQN
 * string per `@CStoryComponent`-annotated function, so that `@CStory.component`
 * can reference it as a real Kotlin symbol instead of a hand-typed string —
 * a typo becomes a compiler `Unresolved reference` error instead of a silent
 * or KSP-time-only failure.
 *
 * The object name is fixed (not derived from the module name): components
 * are meant to be annotated directly in the module that defines them (KSP
 * only scans symbols in the module it runs against, not in dependencies),
 * so a single such module generates at most one `CStoryComponentRefs`. A
 * downstream consumer (e.g. a `@CStory` catalog module) can then simply
 * import it like any other dependency symbol.
 */
internal object ComponentRefsGenerator {
    private const val GENERATED_PACKAGE = "io.cstories.generated"
    const val OBJECT_NAME = "CStoryComponentRefs"
    const val QUALIFIED_NAME = "$GENERATED_PACKAGE.$OBJECT_NAME"

    private val documentationAnnotation = ClassName("io.cstories.annotations", "GeneratedComponentDocumentation")

    fun generate(codeGenerator: CodeGenerator, components: List<ComponentDescriptor>) {
        if (components.isEmpty()) return

        val topLevel = components.filter { it.enclosingObjectName == null }
        val nested = components
            .filter { it.enclosingObjectName != null }
            .groupBy { it.enclosingObjectName!! }

        val rootBuilder = TypeSpec.objectBuilder(OBJECT_NAME)

        topLevel.forEach { component ->
            rootBuilder.addProperty(constProperty(component.functionName, component.fqn, component.documentation))
        }

        nested.forEach { (enclosingName, entries) ->
            val nestedBuilder = TypeSpec.objectBuilder(enclosingName)
            entries.forEach { component ->
                nestedBuilder.addProperty(constProperty(component.functionName, component.fqn, component.documentation))
            }
            rootBuilder.addType(nestedBuilder.build())
        }

        val fileSpec = FileSpec.builder(GENERATED_PACKAGE, OBJECT_NAME)
            .addType(rootBuilder.build())
            .build()

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false,
            originatingKSFiles = components.mapNotNull { it.originatingFile },
        )
    }

    private fun constProperty(name: String, value: String, documentation: String?): PropertySpec {
        val builder = PropertySpec.builder(name, STRING)
            .addModifiers(KModifier.CONST)
            .initializer("%S", value)
        if (documentation != null) {
            builder.addAnnotation(
                AnnotationSpec.builder(documentationAnnotation)
                    .addMember("%S", documentation)
                    .build(),
            )
        }
        return builder.build()
    }
}
