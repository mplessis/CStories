package io.cstories.processor

internal object ComponentNamespaceValidation {
    private val namespaceRegex = Regex("[A-Za-z_][A-Za-z0-9_]*")

    // Matches the set of hard Kotlin keywords, which can never be used as a plain identifier.
    private val reservedKeywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
        "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
        "typeof", "val", "var", "when", "while",
    )

    fun validate(namespace: String): String? {
        if (namespace.isEmpty()) return null
        if (!namespaceRegex.matches(namespace) || namespace in reservedKeywords) {
            return "Invalid @CStoryComponent namespace '$namespace': expected a single valid Kotlin identifier that is not a reserved keyword"
        }
        return null
    }
}
