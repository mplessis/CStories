package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate

class CStoriesProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val moduleName: String,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(CSTORY_ANNOTATION_FQN)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        val deferred = symbols.filterNot(KSAnnotated::validate)
        val entries = symbols
            .filter(KSAnnotated::validate)
            .mapNotNull(::validateAndBuildEntry)

        if (entries.isEmpty()) {
            return deferred
        }

        val registry = StoryRegistryGenerator.generate(codeGenerator, entries, moduleName)
        RegistryManifestWriter.write(codeGenerator, registry, entries.mapNotNull { it.originatingFile })

        return deferred
    }

    private fun validateAndBuildEntry(function: KSFunctionDeclaration): StoryDescriptor? {
        if (!function.isComposable()) {
            logger.error(
                "Function ${function.simpleName.asString()} annotated with @CStory must also be @Composable",
                function,
            )
            return null
        }

        if (function.parameters.any { !it.hasDefault }) {
            logger.error(
                "Story function must have no required parameters: ${function.simpleName.asString()}",
                function,
            )
            return null
        }

        val annotation = function.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedNameAsString() == CSTORY_ANNOTATION_FQN }
            ?: return null
        val group = annotation.stringArgument("group")
        val name = annotation.stringArgument("name")

        val groupValidationError = StoryValidation.validateGroupAndName(group, name)
        if (groupValidationError != null) {
            logger.error(groupValidationError, function)
            return null
        }
        val validatedGroup = checkNotNull(group)
        val validatedName = checkNotNull(name)

        val invoker = when (val parent = function.parentDeclaration) {
            null -> StoryInvoker.TopLevel(
                packageName = function.packageName.asString(),
                functionName = function.simpleName.asString(),
            )

            is KSClassDeclaration if parent.classKind == ClassKind.OBJECT -> StoryInvoker.ObjectMember(
                packageName = parent.packageName.asString(),
                objectName = parent.simpleName.asString(),
                functionName = function.simpleName.asString(),
            )

            else -> {
                logger.error(
                    "@CStory supports top-level functions and object functions only: ${function.simpleName.asString()}",
                    function,
                )
                return null
            }
        }

        return StoryDescriptor(
            group = validatedGroup,
            name = validatedName,
            invoker = invoker,
            originatingFile = function.containingFile,
        )
    }
}

private const val CSTORY_ANNOTATION_FQN = "io.cstories.annotations.CStory"
private const val COMPOSABLE_ANNOTATION_FQN = "androidx.compose.runtime.Composable"

private fun KSFunctionDeclaration.isComposable(): Boolean {
    return annotations.any { it.annotationType.resolve().declaration.qualifiedNameAsString() == COMPOSABLE_ANNOTATION_FQN }
}

private fun KSDeclaration.qualifiedNameAsString(): String? = qualifiedName?.asString()

private fun com.google.devtools.ksp.symbol.KSAnnotation.stringArgument(name: String): String? {
    return arguments.firstOrNull { it.name?.asString() == name }?.value as? String
}
