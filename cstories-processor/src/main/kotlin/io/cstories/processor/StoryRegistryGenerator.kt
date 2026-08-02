package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

internal object StoryRegistryGenerator {
    private const val GENERATED_PACKAGE = "io.cstories.generated"

    private val storyEntryClass = ClassName("io.cstories.runtime", "StoryEntry")
    private val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
    private val listClass = ClassName("kotlin.collections", "List")
    private val lambdaType = LambdaTypeName.get(returnType = com.squareup.kotlinpoet.UNIT)
        .copy(annotations = listOf(AnnotationSpec.builder(composableAnnotation).build()))

    fun generate(
        codeGenerator: CodeGenerator,
        entries: List<StoryDescriptor>,
        moduleName: String,
    ): GeneratedRegistry {
        val objectName = "Generated${sanitizeModuleName(moduleName)}Stories"
        val registry = GeneratedRegistry(
            packageName = GENERATED_PACKAGE,
            objectName = objectName,
        )

        val fileSpec = FileSpec.builder(registry.packageName, registry.objectName)
            .addType(
                TypeSpec.objectBuilder(registry.objectName)
                    .addProperty(
                        PropertySpec.builder(
                            name = "entries",
                            type = listClass.parameterizedBy(storyEntryClass),
                        )
                            .initializer(buildEntriesInitializer(entries))
                            .build(),
                    )
                    .build(),
            )
            .build()

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            // Entries accumulate across KSP rounds/invocations (see
            // `CStoriesProcessor.runStoriesPass`), so this output can't be
            // tied to a specific, precise set of originating `KSFile`s the
            // way a normal per-round aggregating write would be — using
            // `Dependencies(aggregating = true)` with an empty file list
            // would make KSP treat it as depending on *nothing*, and prune
            // it as orphaned on the next incremental run where nothing
            // happens to be individually re-flagged. `Dependencies.ALL_FILES`
            // is KSP's own shorthand for "depends on everything", the
            // correct way to express a whole-module aggregate like this.
            dependencies = Dependencies.ALL_FILES,
        )

        return registry
    }

    internal fun sanitizeModuleName(moduleName: String): String {
        val segments = moduleName
            .trim()
            .ifEmpty { "Default" }
            .split(Regex("[^A-Za-z0-9]+"))
            .filter(String::isNotBlank)
            .ifEmpty { listOf("Default") }

        return buildString {
            segments.forEach { segment ->
                append(segment.replaceFirstChar { it.uppercase() })
            }
        }
    }

    private fun buildEntriesInitializer(entries: List<StoryDescriptor>): CodeBlock {
        val builder = CodeBlock.builder().add("listOf(\n")
        entries.forEachIndexed { index, entry ->
            builder.add("  %T(\n", storyEntryClass)
            builder.add("    path = listOf(")
            entry.pathSegments.forEachIndexed { segmentIndex, segment ->
                if (segmentIndex > 0) {
                    builder.add(", ")
                }
                builder.add("%S", segment)
            }
            builder.add("),\n")
            builder.add("    composableInvoker = %L,\n", buildInvoker(entry.invoker))
            builder.add("    documentation = %L\n", entry.documentation?.let { CodeBlock.of("%S", it) } ?: CodeBlock.of("null"))
            builder.add("  )")
            if (index < entries.lastIndex) {
                builder.add(",")
            }
            builder.add("\n")
        }
        builder.add(")")
        return builder.build()
    }

    private fun buildInvoker(invoker: StoryInvoker): CodeBlock {
        return when (invoker) {
            is StoryInvoker.TopLevel -> CodeBlock.of(
                "{%M()}",
                MemberName(invoker.packageName, invoker.functionName),
            )

            is StoryInvoker.ObjectMember -> CodeBlock.of(
                "{%T.%N()}",
                ClassName(invoker.packageName, invoker.objectName),
                invoker.functionName,
            )
        }
    }
}
