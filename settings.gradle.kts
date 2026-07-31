pluginManagement {
    includeBuild("cstories-gradle-plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    // Lets Gradle automatically download the JetBrains Runtime (JBR) that
    // Compose Hot Reload's `hotRun*` tasks require.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
