package io.cstories.processor

/**
 * Structured information about a `@param`'s resolved type, used to enrich the
 * generated documentation with the concrete set of values/variants a caller
 * can pass, when that can be determined reliably at compile time.
 */
internal sealed interface ParamTypeInfo {
    /** No structural enrichment possible (plain type, open class, non-sealed interface...). */
    data object Plain : ParamTypeInfo

    /** The parameter type is an `enum class`; [entries] lists its enum entries. */
    data class EnumValues(val entries: List<DocumentedEntry>) : ParamTypeInfo

    /** The parameter type is a `sealed class`/`sealed interface`; [subtypes] lists its direct subtypes. */
    data class SealedSubtypes(val subtypes: List<DocumentedEntry>) : ParamTypeInfo
}

/** A named entry (enum entry or sealed subtype) with its own optional KDoc. */
internal data class DocumentedEntry(val name: String, val doc: String?)

/**
 * Full metadata resolved for a single `@param`, used by [KDocMarkdownParser]
 * to render the parameter table: its display type name, whether it is
 * required (no default value), its default value expression (if any and if
 * it could be recovered from the source), and its structural enrichment
 * (if any).
 */
internal data class ParamMetadata(
    val typeName: String,
    val required: Boolean,
    val structural: ParamTypeInfo,
    val defaultValue: String? = null,
)
