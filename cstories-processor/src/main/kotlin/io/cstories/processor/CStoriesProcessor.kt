package io.cstories.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate

/**
 * Runs in up to two passes per invocation, wired by `CStoriesGradlePlugin`:
 *
 * - **Common metadata mode** (`kspCommonMainKotlinMetadata`, [processComponents]
 *   `true`, [processStories] `false`): only processes `@CStoryComponent` to
 *   generate `CStoryComponentRefs`. This *must* run against `commonMain`
 *   metadata (not per-target) so the generated refs object ends up in a
 *   source set visible from `commonMain` — a per-target-only run would
 *   generate it into a platform-specific source set, which `commonMain`
 *   code in the same module is forbidden from referencing.
 * - **Platform mode** (`kspKotlinJvm`/`kspKotlinWasmJs` in a multi-target
 *   project, [processComponents] `false`, [processStories] `true`): only
 *   processes `@CStory`, generating the per-module `StoryEntry` registry.
 * - **Standalone mode** ([processComponents] and [processStories] both
 *   `true`): used for single-target consumers, where Kotlin never creates a
 *   `kspCommonMainKotlinMetadata` task at all (no separate metadata
 *   compilation is needed when only one target consumes `commonMain`) — the
 *   one and only per-target ksp run handles both. `CStoriesGradlePlugin`
 *   still has to move the generated output onto `commonMain` explicitly in
 *   this case: Kotlin enforces the same source-set/fragment boundary
 *   regardless of target count, so a file generated into a platform-only
 *   source set stays invisible from `commonMain` even with a single target.
 *
 * When a story sets `component = ...`, its documentation is resolved by
 * directly looking up (and parsing the KDoc of) the referenced function via
 * this round's own [Resolver] — no state needs to be shared with a separate
 * common-metadata processor run/task.
 */
class CStoriesProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val moduleName: String,
    private val processComponents: Boolean,
    private val processStories: Boolean,
) : SymbolProcessor {

    private val processedComponentKeys = mutableSetOf<String>()

    /**
     * KSP throws `FileAlreadyExistsException` if the same generated file
     * path is written more than once across rounds — the story registry
     * can therefore only ever be written a single time, once every
     * currently-known `@CStory` symbol resolves successfully in the same
     * round (no more deferrals left).
     */
    private var storyRegistryWritten = false

    /** See [runStoriesPass] for why entries accumulate across rounds. */
    private val accumulatedEntries = mutableListOf<StoryDescriptor>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferredComponents = if (processComponents) runComponentsPass(resolver) else emptyList()
        val deferredStories = if (processStories) runStoriesPass(resolver) else emptyList()
        return deferredComponents + deferredStories
    }

    private fun runComponentsPass(resolver: Resolver): List<KSAnnotated> {
        val componentSymbols = resolver
            .getSymbolsWithAnnotation(CSTORY_COMPONENT_ANNOTATION_FQN)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        val deferredComponents = componentSymbols.filterNot(KSAnnotated::validate)
        val newComponents = componentSymbols
            .filter(KSAnnotated::validate)
            .mapNotNull(::validateAndBuildComponent)
            .filter { processedComponentKeys.add(it.refKey) }

        if (newComponents.isNotEmpty()) {
            ComponentRefsGenerator.generate(codeGenerator, newComponents)
        }

        return deferredComponents
    }

    private fun runStoriesPass(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(CSTORY_ANNOTATION_FQN)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        val deferred = mutableListOf<KSAnnotated>()
        deferred.addAll(symbols.filterNot(KSAnnotated::validate))

        // In "standalone" mode, a story's `component = CStoryComponentRefs.Xxx`
        // reference may point at a `@CStoryComponent` processed moments
        // earlier in this very same round: the KSP resolver can't evaluate
        // that constant yet (its containing `CStoryComponentRefs` file was
        // only just generated, not yet visible as a resolvable declaration)
        // — the annotation argument then resolves to `null` instead of its
        // default empty-string value. Deferring the whole symbol here makes
        // KSP schedule another round, by which point the newly generated
        // source is visible and the reference resolves.
        //
        // Once a symbol is *not* deferred this round, KSP's own
        // `getSymbolsWithAnnotation` won't surface it again in a later
        // round — so resolved entries accumulate in [accumulatedEntries]
        // across rounds rather than being recomputed from scratch each
        // time. `StoryDescriptor` deliberately holds no `KSFunctionDeclaration`/
        // `KSFile` reference, so retaining it past the round it was built in
        // is safe (unlike the underlying KSP symbols themselves, whose
        // backing analysis session gets torn down between rounds).
        val resolvableSymbols = symbols.filter(KSAnnotated::validate).filter { function ->
            if (hasUnresolvedComponentReference(function)) {
                deferred += function
                false
            } else {
                true
            }
        }

        accumulatedEntries += resolvableSymbols.mapNotNull { validateAndBuildEntry(it, resolver) }

        // The story registry file can only be written once overall — KSP
        // throws `FileAlreadyExistsException` if the same generated file
        // path is written again in a later round — so it's deferred until
        // no `@CStory` symbol is left needing another round.
        if (accumulatedEntries.isEmpty() || deferred.isNotEmpty() || storyRegistryWritten) {
            return deferred
        }

        storyRegistryWritten = true
        val registry = StoryRegistryGenerator.generate(codeGenerator, accumulatedEntries, moduleName)
        RegistryManifestWriter.write(codeGenerator, registry)

        return deferred
    }

    /**
     * `true` when this story's `@CStory(component = ...)` argument was
     * given an expression but KSP couldn't evaluate it to a constant this
     * round (as opposed to the argument being omitted, which resolves to
     * its default empty-string value instead of `null`).
     */
    private fun hasUnresolvedComponentReference(function: KSFunctionDeclaration): Boolean {
        val annotation = function.annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedNameAsString() == CSTORY_ANNOTATION_FQN }
            ?: return false
        val argument = annotation.arguments.firstOrNull { it.name?.asString() == "component" } ?: return false
        return argument.value == null
    }

    private data class FunctionLocation(
        val packageName: String,
        /** Display name only (e.g. `Companion`), used to group generated refs. */
        val enclosingObjectName: String?,
        /**
         * Full dotted path of enclosing declarations relative to the package
         * (e.g. `LumenButton.Companion`), used to build a resolvable FQN.
         * `null` for top-level functions.
         */
        val enclosingQualifiedPath: String?,
    )

    private fun resolveFunctionLocation(function: KSFunctionDeclaration): FunctionLocation? {
        return when (val parent = function.parentDeclaration) {
            null -> FunctionLocation(
                packageName = function.packageName.asString(),
                enclosingObjectName = null,
                enclosingQualifiedPath = null,
            )

            is KSClassDeclaration if parent.classKind == ClassKind.OBJECT -> {
                val packageName = parent.packageName.asString()
                val enclosingQualifiedPath = generateSequence(parent as KSDeclaration) { it.parentDeclaration }
                    .toList()
                    .asReversed()
                    .joinToString(".") { it.simpleName.asString() }
                FunctionLocation(
                    packageName = packageName,
                    enclosingObjectName = parent.simpleName.asString(),
                    enclosingQualifiedPath = enclosingQualifiedPath,
                )
            }

            else -> null
        }
    }

    private fun validateAndBuildComponent(function: KSFunctionDeclaration): ComponentDescriptor? {
        val location = resolveFunctionLocation(function)
        if (location == null) {
            logger.error(
                "@CStoryComponent supports top-level functions and object functions only: ${function.simpleName.asString()}",
                function,
            )
            return null
        }

        val fqn = buildString {
            append(location.packageName)
            append('.')
            location.enclosingQualifiedPath?.let { append(it).append('.') }
            append(function.simpleName.asString())
        }

        val descriptor = ComponentDescriptor(
            enclosingObjectName = location.enclosingObjectName,
            functionName = function.simpleName.asString(),
            fqn = fqn,
            function = function,
            originatingFile = function.containingFile,
            documentation = parseFunctionDocumentation(function),
        )

        if (processedComponentKeys.contains(descriptor.refKey)) {
            logger.error(
                "@CStoryComponent name collision for '${descriptor.refKey}': another component already uses this reference name",
                function,
            )
            return null
        }

        return descriptor
    }

    /** Resolves the `@CStoryComponent` function referenced by a story's `component` FQN, top-level or object member. */
    private fun resolveComponentFunction(resolver: Resolver, fqn: String): KSFunctionDeclaration? {
        resolver.getFunctionDeclarationsByName(fqn, includeTopLevel = true).firstOrNull()?.let { return it }

        val lastDot = fqn.lastIndexOf('.')
        if (lastDot == -1) return null
        val enclosingFqn = fqn.substring(0, lastDot)
        val functionName = fqn.substring(lastDot + 1)
        val enclosing = resolver.getClassDeclarationByName(enclosingFqn) ?: return null
        return enclosing.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .firstOrNull { it.simpleName.asString() == functionName }
    }

    private fun resolveDocumentation(resolver: Resolver, fqn: String, story: KSFunctionDeclaration): String? {
        val componentFunction = resolveComponentFunction(resolver, fqn)
        if (componentFunction == null) {
            logger.error("@CStory 'component' does not resolve to a function: $fqn", story)
            return null
        }

        return parseFunctionDocumentation(componentFunction)
            ?: resolveDocumentationFromGeneratedRefs(resolver, componentFunction)
    }

    /**
     * Parses [function]'s own `docString` directly. Only ever returns
     * non-null when [function] was declared in the module currently being
     * processed — KSP never exposes `docString` for a symbol resolved from
     * a dependency module (KDoc comments are source-only, stripped from
     * `.class` files), regardless of how that symbol was reached.
     */
    private fun parseFunctionDocumentation(function: KSFunctionDeclaration): String? {
        val parsed = KDocMarkdownParser.parse(function.docString, ParamTypeResolver.resolve(function)) ?: return null
        parsed.unsupportedTags.forEach { tag ->
            logger.warn(
                "@CStoryComponent: KDoc tag '@$tag' on ${function.simpleName.asString()} is not supported and will be ignored",
                function,
            )
        }
        return parsed.markdown
    }

    /**
     * Fallback for a `@CStoryComponent` declared in a different module than
     * the `@CStory` referencing it. `CStoriesComponentsGradlePlugin`
     * (applied directly on that other module, where the KDoc source is
     * still visible) bakes the already-rendered Markdown into a
     * `@GeneratedComponentDocumentation` annotation on the matching
     * `CStoryComponentRefs` property — read it back here instead, since
     * annotations (unlike `docString`) do survive across the module
     * boundary.
     */
    private fun resolveDocumentationFromGeneratedRefs(resolver: Resolver, function: KSFunctionDeclaration): String? {
        val location = resolveFunctionLocation(function) ?: return null
        val refsClassName = location.enclosingObjectName
            ?.let { "${ComponentRefsGenerator.QUALIFIED_NAME}.$it" }
            ?: ComponentRefsGenerator.QUALIFIED_NAME
        val refsClass = resolver.getClassDeclarationByName(refsClassName) ?: return null
        val property = refsClass.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .firstOrNull { it.simpleName.asString() == function.simpleName.asString() }
            ?: return null
        val annotation = property.annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedNameAsString() == GENERATED_DOC_ANNOTATION_FQN }
            ?: return null
        return annotation.stringArgument("markdown")
    }

    private fun validateAndBuildEntry(function: KSFunctionDeclaration, resolver: Resolver): StoryDescriptor? {
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
        val collection = annotation.stringArgument("collection")
        val group = annotation.stringArgument("group")
        val name = annotation.stringArgument("name")
        val component = annotation.stringArgument("component")

        val validationError = StoryValidation.validateCollectionGroupAndName(collection, group, name)
        if (validationError != null) {
            logger.error(validationError, function)
            return null
        }
        val validatedCollection = checkNotNull(collection)
        val validatedGroup = checkNotNull(group)
        val validatedName = checkNotNull(name)

        val location = resolveFunctionLocation(function)
        val invoker = when {
            location == null -> {
                logger.error(
                    "@CStory supports top-level functions and object functions only: ${function.simpleName.asString()}",
                    function,
                )
                return null
            }

            location.enclosingObjectName == null -> StoryInvoker.TopLevel(
                packageName = location.packageName,
                functionName = function.simpleName.asString(),
            )

            else -> StoryInvoker.ObjectMember(
                packageName = location.packageName,
                objectName = location.enclosingObjectName,
                functionName = function.simpleName.asString(),
            )
        }

        val documentation = component
            ?.takeIf { it.isNotBlank() }
            ?.let { fqn -> resolveDocumentation(resolver, fqn, function) }

        return StoryDescriptor(
            collection = validatedCollection,
            group = validatedGroup,
            name = validatedName,
            invoker = invoker,
            documentation = documentation,
        )
    }
}

private const val CSTORY_ANNOTATION_FQN = "io.cstories.annotations.CStory"
private const val CSTORY_COMPONENT_ANNOTATION_FQN = "io.cstories.annotations.CStoryComponent"
private const val COMPOSABLE_ANNOTATION_FQN = "androidx.compose.runtime.Composable"
private const val GENERATED_DOC_ANNOTATION_FQN = "io.cstories.annotations.GeneratedComponentDocumentation"

private fun KSFunctionDeclaration.isComposable(): Boolean {
    return annotations.any { it.annotationType.resolve().declaration.qualifiedNameAsString() == COMPOSABLE_ANNOTATION_FQN }
}

private fun KSDeclaration.qualifiedNameAsString(): String? = qualifiedName?.asString()

private fun com.google.devtools.ksp.symbol.KSAnnotation.stringArgument(name: String): String? {
    return arguments.firstOrNull { it.name?.asString() == name }?.value as? String
}
