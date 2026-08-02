package io.cstories.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import java.io.File

/**
 * Resolves the [ParamTypeInfo] of each parameter of a documented component
 * function, so [KDocMarkdownParser] can enrich `@param` entries with the
 * concrete set of values (enums) or variants (sealed types) a caller can pass.
 */
internal object ParamTypeResolver {
    /** Matches KSP's synthetic `kotlin.Function0`..`kotlin.FunctionN` / `kotlin.SuspendFunctionN` declarations. */
    private val functionTypeRegex = Regex("^kotlin\\.(Function|SuspendFunction)\\d+$")

    fun resolve(function: KSFunctionDeclaration): Map<String, ParamMetadata> {
        val result = mutableMapOf<String, ParamMetadata>()
        function.parameters.forEach { parameter ->
            val name = parameter.name?.asString() ?: return@forEach
            val type = parameter.type.resolve()
            val declaration = type.declaration as? KSClassDeclaration
            val structural = declaration?.let(::resolveDeclaration) ?: ParamTypeInfo.Plain
            result[name] = ParamMetadata(
                typeName = typeNameOf(type),
                required = !parameter.hasDefault,
                structural = structural,
                defaultValue = defaultValueOf(parameter, name),
            )
        }
        return result
    }

    /**
     * Best-effort recovery of a parameter's default value expression as
     * source text. KSP does not expose the default value expression through
     * its public API, so this reads the declaring source file at the
     * parameter's own location and extracts the text following `=` up to
     * the next top-level comma or closing bracket. Returns `null` when the
     * parameter has no default, or when the expression could not be
     * recovered (e.g. it spans multiple lines).
     */
    private fun defaultValueOf(parameter: KSValueParameter, name: String): String? {
        if (!parameter.hasDefault) return null
        val location = parameter.location as? FileLocation ?: return null
        return runCatching {
            val line = File(location.filePath).useLines { lines ->
                lines.drop(location.lineNumber - 1).firstOrNull()
            } ?: return@runCatching null
            val nameIndex = line.indexOf(name)
            if (nameIndex == -1) return@runCatching null
            val eqIndex = line.indexOf('=', nameIndex)
            if (eqIndex == -1) return@runCatching null
            extractDefaultExpression(line.substring(eqIndex + 1))
        }.getOrNull()
    }

    /** Extracts a single expression from [text], stopping at the first top-level `,`, `)`, `]` or `}`. */
    private fun extractDefaultExpression(text: String): String? {
        var depth = 0
        val sb = StringBuilder()
        for (c in text) {
            when (c) {
                '(', '[', '{' -> depth++
                ')', ']', '}' -> {
                    if (depth == 0) return sb.toString().trim().takeIf { it.isNotBlank() }
                    depth--
                }
                ',' -> if (depth == 0) return sb.toString().trim().takeIf { it.isNotBlank() }
            }
            sb.append(c)
        }
        return sb.toString().trim().takeIf { it.isNotBlank() }
    }

    /** Renders a [KSType] as a readable Kotlin type name, expanding lambda types to `(A, B) -> R` syntax. */
    private fun typeNameOf(type: KSType): String {
        val declaration = type.declaration as? KSClassDeclaration
        val functionMatch = declaration?.qualifiedName?.asString()?.let { functionTypeRegex.matches(it) } == true

        val base = if (functionMatch) {
            val isSuspend = declaration.qualifiedName?.asString()?.contains("Suspend") == true
            val argumentTypes = type.arguments.mapNotNull { it.type?.resolve() }
            val parameterTypes = argumentTypes.dropLast(1)
            val returnType = argumentTypes.lastOrNull()
            val parametersRendered = parameterTypes.joinToString(", ") { typeNameOf(it) }
            val returnRendered = returnType?.let(::typeNameOf) ?: "Unit"
            val prefix = if (isSuspend) "suspend " else ""
            "$prefix($parametersRendered) -> $returnRendered"
        } else {
            declaration?.simpleName?.asString() ?: type.toString()
        }

        return when {
            !type.isMarkedNullable -> base
            functionMatch -> "($base)?"
            else -> "$base?"
        }
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
