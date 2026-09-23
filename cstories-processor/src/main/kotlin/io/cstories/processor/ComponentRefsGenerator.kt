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
import java.io.OutputStreamWriter
import java.util.Base64

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
    const val METADATA_PATH = "META-INF/cstories/components.txt"

    private val documentationAnnotation = ClassName("io.cstories.annotations", "GeneratedComponentDocumentation")

    fun generate(codeGenerator: CodeGenerator, components: List<ComponentDescriptor>) {
        if (components.isEmpty()) return

        val fileSpec = buildFileSpec(components)

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false,
            originatingKSFiles = components.mapNotNull { it.originatingFile },
        )
    }

    fun writeMetadata(codeGenerator: CodeGenerator, components: List<ComponentDescriptor>) {
        if (components.isEmpty()) return
        codeGenerator.createNewFile(
            dependencies = com.google.devtools.ksp.processing.Dependencies.ALL_FILES,
            packageName = "",
            fileName = METADATA_PATH.removeSuffix(".txt"),
            extensionName = "txt",
        ).use { output ->
            OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                components.forEach { component ->
                    writer.appendLine(encode(component.namespace))
                    writer.appendLine(encode(component.enclosingObjectName ?: ""))
                    writer.appendLine(encode(component.functionName))
                    writer.appendLine(encode(component.fqn))
                    writer.appendLine(encode(component.documentation ?: ""))
                }
            }
        }
    }

    fun decodeMetadata(lines: List<String>): List<ComponentMetadata> {
        return lines.filter(String::isNotBlank).chunked(5).mapNotNull { fields ->
            if (fields.size != 5) return@mapNotNull null
            ComponentMetadata(
                namespace = decode(fields[0]),
                enclosingObjectName = decode(fields[1]).ifEmpty { null },
                functionName = decode(fields[2]),
                fqn = decode(fields[3]),
                documentation = decode(fields[4]).ifEmpty { null },
            )
        }
    }

    fun generateFromMetadata(codeGenerator: CodeGenerator, metadata: List<ComponentMetadata>) {
        if (metadata.isEmpty()) return
        generateDescriptors(codeGenerator, metadata.map { it.toDescriptor() })
    }

    fun generateDescriptors(codeGenerator: CodeGenerator, components: List<ComponentDescriptor>) {
        if (components.isEmpty()) return
        buildFileSpec(components).writeTo(
            codeGenerator = codeGenerator,
            aggregating = true,
        )
    }

    internal fun ComponentMetadata.toDescriptor() = ComponentDescriptor(
        namespace = namespace,
        enclosingObjectName = enclosingObjectName,
        functionName = functionName,
        fqn = fqn,
        function = null,
        originatingFile = null,
        documentation = documentation,
    )

    private fun encode(value: String): String = Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String = String(Base64.getDecoder().decode(value), Charsets.UTF_8)

    data class ComponentMetadata(
        val namespace: String,
        val enclosingObjectName: String?,
        val functionName: String,
        val fqn: String,
        val documentation: String?,
    )

    internal fun buildFileSpec(components: List<ComponentDescriptor>): FileSpec {
        val rootNode = buildReferenceTree(components)
        val rootBuilder = TypeSpec.objectBuilder(OBJECT_NAME)

        rootNode.properties.forEach { component ->
            rootBuilder.addProperty(constProperty(component.functionName, component.fqn, component.documentation))
        }

        rootNode.children.values.forEach { child ->
            rootBuilder.addType(child.toTypeSpec())
        }

        return FileSpec.builder(GENERATED_PACKAGE, OBJECT_NAME)
            .addType(rootBuilder.build())
            .build()
    }

    private fun constProperty(name: String, value: String, documentation: String?): PropertySpec {
        val builder = PropertySpec.builder(name, STRING)
            .addModifiers(KModifier.CONST)
            .initializer("%S", value)
        if (documentation != null) {
            builder.addAnnotation(
                AnnotationSpec.builder(documentationAnnotation)
                    .addMember("%S", documentation)
                    .addMember("componentFqn = %S", value)
                    .build(),
            )
        }
        return builder.build()
    }

    internal fun validateStructure(components: List<ComponentDescriptor>): List<String> {
        val rootNode = RefNode(name = null, path = emptyList())
        val errors = mutableListOf<String>()
        components.forEach { component ->
            rootNode.insert(component, errors)
        }
        return errors.distinct()
    }

    private fun buildReferenceTree(components: List<ComponentDescriptor>): RefNode {
        val rootNode = RefNode(name = null, path = emptyList())
        val errors = mutableListOf<String>()
        components.forEach { component -> rootNode.insert(component, errors) }
        check(errors.isEmpty()) {
            "Invalid component reference structure: ${errors.joinToString()}"
        }
        return rootNode
    }

    private data class RefNode(
        val name: String?,
        val path: List<String>,
        val children: LinkedHashMap<String, RefNode> = linkedMapOf(),
        val properties: MutableList<ComponentDescriptor> = mutableListOf(),
    ) {
        fun insert(component: ComponentDescriptor, errors: MutableList<String>) {
            val parentSegments = component.refPathSegments.dropLast(1)
            var node = this
            parentSegments.forEach { segment ->
                if (node.properties.any { it.functionName == segment }) {
                    errors += "@CStoryComponent reference collision for '${(node.path + segment).joinToString(".")}': a property already exists at that path"
                    return
                }
                node = node.children.getOrPut(segment) {
                    RefNode(name = segment, path = node.path + segment)
                }
            }

            if (node.children.containsKey(component.functionName)) {
                errors += "@CStoryComponent reference collision for '${component.refKey}': an object already exists at that path"
                return
            }

            if (node.properties.any { it.functionName == component.functionName }) {
                errors += "@CStoryComponent reference collision for '${component.refKey}': another component already uses this reference name"
                return
            }

            node.properties += component
        }

        fun toTypeSpec(): TypeSpec {
            val nestedBuilder = TypeSpec.objectBuilder(checkNotNull(name))
            properties.forEach { component ->
                nestedBuilder.addProperty(constProperty(component.functionName, component.fqn, component.documentation))
            }
            children.values.forEach { child ->
                nestedBuilder.addType(child.toTypeSpec())
            }
            return nestedBuilder.build()
        }
    }
}
