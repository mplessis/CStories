package io.cstories.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Resolves the [ParamTypeInfo] of each parameter of a documented component
 * function, so [KDocMarkdownParser] can enrich `@param` entries with the
 * concrete set of values (enums) or variants (sealed types) a caller can pass.
 */
internal object ParamTypeResolver {
    fun resolve(function: KSFunctionDeclaration): Map<String, ParamTypeInfo> {
        val result = mutableMapOf<String, ParamTypeInfo>()
        function.parameters.forEach { parameter ->
            val name = parameter.name?.asString() ?: return@forEach
            val declaration = parameter.type.resolve().declaration as? KSClassDeclaration ?: return@forEach
            result[name] = resolveDeclaration(declaration)
        }
        return result
    }

    private fun resolveDeclaration(declaration: KSClassDeclaration): ParamTypeInfo {
        return when {
            declaration.classKind == ClassKind.ENUM_CLASS -> {
                val entries = declaration.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .map { it.toDocumentedEntry() }
                    .toList()
                if (entries.isNotEmpty()) ParamTypeInfo.EnumValues(entries) else ParamTypeInfo.Plain
            }

            Modifier.SEALED in declaration.modifiers -> {
                val subtypes = declaration.getSealedSubclasses()
                    .map { it.toDocumentedEntry() }
                    .toList()
                if (subtypes.isNotEmpty()) ParamTypeInfo.SealedSubtypes(subtypes) else ParamTypeInfo.Plain
            }

            else -> ParamTypeInfo.Plain
        }
    }

    private fun KSClassDeclaration.toDocumentedEntry(): DocumentedEntry =
        DocumentedEntry(
            name = simpleName.asString(),
            doc = docString?.let(KDocMarkdownParser::cleanDocString)?.takeIf { it.isNotBlank() },
        )
}
