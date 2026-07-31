package io.cstories.processor

internal object StoryValidation {
    fun validateCollectionGroupAndName(collection: String?, group: String?, name: String?): String? {
        if (collection.isNullOrBlank() || group.isNullOrBlank() || name.isNullOrBlank()) {
            return "@CStory collection, group and name must not be blank"
        }
        if (name.contains('/')) {
            return "@CStory 'name' must not contain '/', use 'group' for hierarchy: $name"
        }
        if (collection.split('/').any(String::isBlank)) {
            return "@CStory 'collection' must not contain empty segments: $collection"
        }
        if (group.split('/').any(String::isBlank)) {
            return "@CStory 'group' must not contain empty segments: $group"
        }
        return null
    }
}
