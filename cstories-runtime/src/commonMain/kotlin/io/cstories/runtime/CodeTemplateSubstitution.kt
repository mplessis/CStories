package io.cstories.runtime

/**
 * Result of [substituteKnobValues]: the story's usage code with knob values
 * substituted in, and the character ranges (in [text]) that were replaced,
 * so [CodeBlock] can highlight them.
 */
internal data class CodeSubstitutionResult(val text: String, val highlightRanges: List<IntRange>)

/**
 * Replaces whole-word occurrences of each key in [values] within [code]
 * with its associated Kotlin literal, so the "Code" tab reflects the
 * story's current knob values instead of the raw variable names captured
 * at build time by the KSP processor.
 *
 * This is a lightweight text substitution (matching [io.cstories.processor]'s
 * own text-based extraction approach), not a real AST rewrite — it may
 * over-match identical identifiers used in unrelated contexts within the
 * same snippet (e.g. shadowed names), which is an accepted limitation.
 */
internal fun substituteKnobValues(code: String, values: Map<String, String>): CodeSubstitutionResult {
    if (values.isEmpty()) return CodeSubstitutionResult(code, emptyList())

    val result = StringBuilder()
    val ranges = mutableListOf<IntRange>()
    var i = 0
    while (i < code.length) {
        val match = values.entries.firstOrNull { (key, _) -> code.matchesIdentifierAt(i, key) }
        if (match != null) {
            val start = result.length
            result.append(match.value)
            ranges += start until (start + match.value.length)
            i += match.key.length
        } else {
            result.append(code[i])
            i++
        }
    }
    return CodeSubstitutionResult(result.toString(), ranges)
}

/** Whether [identifier] occurs at [index] in this string as a whole identifier (respecting word boundaries on both sides). */
private fun String.matchesIdentifierAt(index: Int, identifier: String): Boolean {
    if (!startsWith(identifier, index)) return false
    val before = getOrNull(index - 1)
    val after = getOrNull(index + identifier.length)
    val boundaryBefore = before == null || !(before.isLetterOrDigit() || before == '_')
    val boundaryAfter = after == null || !(after.isLetterOrDigit() || after == '_')
    return boundaryBefore && boundaryAfter
}
