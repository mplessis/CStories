package io.cstories.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * Documentation of a declaration explicitly referenced from a component's
 * KDoc (via a `[Symbol]` link or a `@see Symbol` tag), rendered as an
 * anchored section below the component's own documentation.
 *
 * @property anchor Anchor id the resolved [linkTexts] point to.
 * @property title Section title, the referenced declaration's simple name.
 * @property docString Raw KDoc of the referenced declaration.
 * @property linkTexts Link texts found in the component KDoc resolving to this declaration (e.g. `TabItem`, `TabItem.count`).
 */
internal data class RelatedDoc(
    val anchor: String,
    val title: String,
    val docString: String,
    val linkTexts: Set<String>,
)

/**
 * Resolves the explicit KDoc links of a `@CStoryComponent` function to the
 * public, documented declarations they point to. Only declarations whose
 * source is part of the module being processed can be resolved, since KSP
 * never exposes `docString` for symbols coming from a dependency.
 *
 * Lookup order: the component's own file, then its package, then the link
 * text taken as a fully qualified name. Links to the component's own
 * parameters and unresolvable links are ignored (rendered as inline code).
 */
internal object KDocLinkResolver {
    private val linkRegex = Regex("\\[([^]\\[]+)](?!\\()")
    private val seeRegex = Regex("@see\\s+(\\S+)")

    fun resolve(resolver: Resolver, function: KSFunctionDeclaration): List<RelatedDoc> {
        val docString = function.docString ?: return emptyList()
        val paramNames = function.parameters.mapNotNull { it.name?.asString() }.toSet()
        val linkTexts = (extractLinkTexts(docString)).filter { it.substringBefore('.') !in paramNames }

        val byDeclaration = linkedMapOf<String, Pair<KSDeclaration, MutableSet<String>>>()
        linkTexts.forEach { text ->
            val declaration = lookup(resolver, function, text) ?: return@forEach
            if (declaration == function) return@forEach
            if (!declaration.isPublic() || declaration.docString.isNullOrBlank()) return@forEach
            val key = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
            byDeclaration.getOrPut(key) { declaration to linkedSetOf() }.second += text
        }

        return byDeclaration.values.map { (declaration, texts) ->
            val name = declaration.simpleName.asString()
            RelatedDoc(anchor = name, title = name, docString = declaration.docString.orEmpty(), linkTexts = texts)
        }
    }

    internal fun extractLinkTexts(docString: String): List<String> =
        (linkRegex.findAll(docString).map { it.groupValues[1].trim() } +
            seeRegex.findAll(docString).map { it.groupValues[1].trim().removeSurrounding("[", "]") })
            .filter { it.isNotEmpty() && it.all { c -> c.isLetterOrDigit() || c == '.' || c == '_' } }
            .distinct()
            .toList()

    private fun lookup(resolver: Resolver, function: KSFunctionDeclaration, text: String): KSDeclaration? {
        val head = text.substringBefore('.')
        function.containingFile?.declarations
            ?.firstOrNull { it.simpleName.asString() == head }
            ?.let { return it }

        val packageName = function.packageName.asString()
        val qualifiedHead = if (packageName.isEmpty()) head else "$packageName.$head"
        byQualifiedName(resolver, qualifiedHead)?.let { return it }

        if ('.' in text) {
            // Fully qualified link, possibly followed by a member: try the longest prefix first.
            val segments = text.split('.')
            for (end in segments.size downTo 2) {
                byQualifiedName(resolver, segments.subList(0, end).joinToString("."))?.let { return it }
            }
        }
        return null
    }

    private fun byQualifiedName(resolver: Resolver, name: String): KSDeclaration? =
        resolver.getClassDeclarationByName(name)
            ?: resolver.getFunctionDeclarationsByName(name, includeTopLevel = true)
                .firstOrNull { !it.docString.isNullOrBlank() }
}
