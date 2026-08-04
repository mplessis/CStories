import java.util.Properties

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

// This is a separate, included Gradle build (see the root project's
// settings.gradle.kts `pluginManagement { includeBuild(...) }`), so it can't
// read Gradle properties from the root `gradle.properties` automatically —
// parse that same file directly instead of duplicating these values.
val rootGradleProperties = Properties().apply {
    file("../gradle.properties").inputStream().use(::load)
}
val cstoriesGroup: String = rootGradleProperties.getProperty("cstoriesGroup")
val cstoriesVersion: String = rootGradleProperties.getProperty("cstoriesVersion")

group = cstoriesGroup
version = cstoriesVersion

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.multiplatform.get()}")
    implementation("org.jetbrains.compose.hot-reload:org.jetbrains.compose.hot-reload.gradle.plugin:${libs.versions.compose.hot.reload.get()}")
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
        create("cstoriesComponents") {
            id = "io.cstories.gradle.components"
            implementationClass = "io.cstories.gradle.CStoriesComponentsGradlePlugin"
            displayName = "CStories Components Gradle Plugin"
            description = "Generates CStoryComponentRefs for @CStoryComponent-annotated functions " +
                "in a plain component/design-system library module"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

/**
 * The plugin needs to know, at runtime in a *consumer's* build, which
 * version of the published `io.cstories:*` artifacts to depend on when no
 * local sibling subproject is found (see `CStoriesGradlePlugin`'s
 * `localProjectOrCoordinates`). That value has to be baked into the
 * compiled plugin at build time — a consumer's build has no access to this
 * repository's `gradle.properties` — so it's generated as a small Kotlin
 * source file here, from the same shared `gradle.properties`.
 */
val generateCstoriesVersion = tasks.register("generateCstoriesVersion") {
    val outputDir = layout.buildDirectory.dir("generated/cstoriesVersion/kotlin")
    outputs.dir(outputDir)
    inputs.property("cstoriesVersion", cstoriesVersion)

    doLast {
        val packageDir = outputDir.get().dir("io/cstories/gradle").asFile.apply { mkdirs() }
        File(packageDir, "CStoriesVersion.kt").writeText(
            """
            |package io.cstories.gradle
            |
            |internal const val CSTORIES_VERSION = "$cstoriesVersion"
            |
            """.trimMargin(),
        )
    }
}

kotlin.sourceSets.getByName("main").kotlin.srcDir(generateCstoriesVersion.map { it.outputs.files.singleFile })
