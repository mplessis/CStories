pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "CStories"

include(
    ":cstories-annotations",
    ":cstories-processor",
    ":cstories-runtime",
    ":cstories-gradle-plugin",
    ":sample",
)
