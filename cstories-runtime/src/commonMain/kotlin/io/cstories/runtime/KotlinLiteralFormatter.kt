package io.cstories.runtime

/**
 * Formats a knob's current value as the Kotlin source literal that would
 * represent it in code (e.g. `"Click me"` for a [String], `true` for a
 * [Boolean]), used by [DocsCodeTabs] to substitute knob values into a
 * story's usage code snippet.
 */
internal fun formatAsKotlinLiteral(value: Any?): String = when (value) {
    null -> "null"
    is String -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    is Boolean, is Int, is Long, is Double, is Float -> value.toString()
    else -> value.toString()
}
