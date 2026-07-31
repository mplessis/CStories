plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

// This is a separate, included Gradle build (see the root project's
// settings.gradle.kts `pluginManagement { includeBuild(...) }`), so it
// doesn't inherit `group`/`version` from the root build's `allprojects`
// block — keep this in sync with the root `build.gradle.kts` version.
group = "io.cstories"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.multiplatform.get()}")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}

gradlePlugin {
    plugins {
        create("cstories") {
            id = "io.cstories.gradle"
            implementationClass = "io.cstories.gradle.CStoriesGradlePlugin"
            displayName = "CStories Gradle Plugin"
            description = "Configures CStories for Compose Multiplatform projects"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
