pluginManagement {
    includeBuild("cstories-gradle-plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    // Kotlin/Wasm and the Node toolchain add their own distribution repository
    // during task setup, so a strict settings-only policy breaks web runs.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
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
    ":sample",
)
