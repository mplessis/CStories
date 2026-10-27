package io.cstories.gradle

import java.io.File
import kotlin.test.assertTrue

/**
 * A minimal KMP consumer (`lib` components module + `lib:stories` catalog
 * module, both on `jvm` and `wasmJs`) built against this repository through
 * composite builds, shared by the TestKit functional tests.
 */
internal class ComponentsConsumerProject(val projectDir: File) {
    private val repoRoot = File(requiredProperty("cstories.repoRoot"))
    private val kotlinVersion = requiredProperty("cstories.kotlinVersion")
    private val composeVersion = requiredProperty("cstories.composeVersion")

    val componentFile: File get() = projectDir.resolve("lib/src/commonMain/kotlin/demo/Alpha.kt")

    fun writeComponentDocumentation(documentation: String) {
        componentFile.apply { parentFile.mkdirs() }.writeText(
            """
            package demo

            import androidx.compose.runtime.Composable
            import io.cstories.annotations.CStoryComponent

            /** $documentation */
            @CStoryComponent
            @Composable
            fun Alpha() {}
            """.trimIndent(),
        )
    }

    fun assertRefsContain(documentation: String) {
        val refs = projectDir.resolve("lib/stories/build/generated").walkTopDown()
            .filter { it.name == "CStoryComponentRefs.kt" }
            .map(File::readText)
            .toList()
        assertTrue(refs.isNotEmpty(), "No CStoryComponentRefs generated in the stories module")
        assertTrue(
            refs.all { documentation in it },
            "CStoryComponentRefs is missing '$documentation':\n${refs.joinToString("\n---\n")}",
        )
    }

    fun write(rootProjectName: String) {
        val root = repoRoot.absolutePath.replace("\\", "/")
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                includeBuild("$root/cstories-gradle-plugin")
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    google()
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                }
            }
            includeBuild("$root")
            rootProject.name = "$rootProjectName"
            include(":lib", ":lib:stories")
            """.trimIndent(),
        )
        projectDir.resolve("gradle.properties").writeText("org.gradle.jvmargs=-Xmx2g\nkotlin.daemon.jvmargs=-Xmx2g\n")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform") version "$kotlinVersion" apply false
                id("org.jetbrains.compose") version "$composeVersion" apply false
                id("org.jetbrains.kotlin.plugin.compose") version "$kotlinVersion" apply false
            }
            """.trimIndent(),
        )
        projectDir.resolve("lib").mkdirs()
        projectDir.resolve("lib/build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform")
                id("org.jetbrains.compose")
                id("org.jetbrains.kotlin.plugin.compose")
                id("dev.cstories.gradle.components")
            }

            @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
            kotlin {
                jvm()
                wasmJs { browser() }
                sourceSets {
                    commonMain.dependencies {
                        implementation(org.jetbrains.compose.ComposePlugin.Dependencies(project).runtime)
                    }
                }
            }
            """.trimIndent(),
        )
        writeComponentDocumentation("Original alpha docs.")

        projectDir.resolve("lib/stories").mkdirs()
        projectDir.resolve("lib/stories/build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform")
                id("org.jetbrains.compose")
                id("org.jetbrains.kotlin.plugin.compose")
                id("dev.cstories.gradle")
            }

            @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
            kotlin {
                jvm()
                wasmJs {
                    browser()
                    binaries.executable()
                }
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":lib"))
                    }
                }
            }
            """.trimIndent(),
        )
        projectDir.resolve("lib/stories/src/commonMain/kotlin/demo/stories").apply { mkdirs() }
            .resolve("AlphaStories.kt").writeText(
                """
                package demo.stories

                import androidx.compose.runtime.Composable
                import demo.Alpha
                import io.cstories.annotations.CStory
                import io.cstories.generated.CStoryComponentRefs

                @CStory(collection = "Test", group = "Components", name = "Alpha", component = CStoryComponentRefs.Alpha)
                @Composable
                fun AlphaStory() {
                    Alpha()
                }
                """.trimIndent(),
            )
    }

    private fun requiredProperty(name: String): String =
        checkNotNull(System.getProperty(name)) { "Missing system property '$name', set by the test task" }
}
