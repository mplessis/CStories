package io.cstories.processor

internal object StoryValidation {
    fun validateGroupAndName(group: String?, name: String?): String? {
        if (group.isNullOrBlank() || name.isNullOrBlank()) {
            return "@CStory group and name must not be blank"
        }
        if (name.contains('/')) {
            return "@CStory 'name' must not contain '/', use 'group' for hierarchy: $name"
        }
        if (group.split('/').any(String::isBlank)) {
            return "@CStory 'group' must not contain empty segments: $group"
        }
        return null
    }
}
