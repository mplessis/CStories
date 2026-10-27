package io.cstories.gradle

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Reproduces a components module with several targets (so KMP packages its
 * `commonMain` resources only into the metadata jar) consumed by a wasmJs-only
 * stories module, with the component declared in a companion object.
 */
class SeparateComponentsModuleTest {
    private val repoRoot = File(requiredProperty("cstories.repoRoot"))
    private val kotlinVersion = requiredProperty("cstories.kotlinVersion")
    private val composeVersion = requiredProperty("cstories.composeVersion")

    @Test
    fun `wasmJs only stories module gets refs for a companion component of a multi target module`() {
        val projectDir = Files.createTempDirectory("cstories-separate-components").toFile()
        try {
            writeProject(projectDir)

            val result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":design-system:stories:compileKotlinWasmJs", "--stacktrace")
                .forwardOutput()
                .build()

            val refs = projectDir.resolve("design-system/stories/build/generated").walkTopDown()
                .single { it.name == "CStoryComponentRefs.kt" }
                .readText()
            assertTrue("object SynButton" in refs, refs)
            assertTrue("const val Primary: String = \"designSystem.SynButton.Companion.Primary\"" in refs, refs)
            assertTrue("no CStories component metadata" !in result.output)
        } finally {
            projectDir.deleteRecursively()
        }
    }

    private fun writeProject(projectDir: File) {
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
            rootProject.name = "separate-components"
            include(":design-system", ":design-system:stories")
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
        projectDir.resolve("design-system/build.gradle.kts").apply { parentFile.mkdirs() }.writeText(
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
                js { browser() }
                wasmJs { browser() }
                sourceSets {
                    commonMain.dependencies {
                        implementation(org.jetbrains.compose.ComposePlugin.Dependencies(project).runtime)
                    }
                }
            }
            """.trimIndent(),
        )
        projectDir.resolve("design-system/src/commonMain/kotlin/designSystem/SynButton.kt").apply { parentFile.mkdirs() }
            .writeText(
                """
                package designSystem

                import androidx.compose.runtime.Composable
                import io.cstories.annotations.CStoryComponent

                class SynButton {
                    companion object {
                        /** Primary button. */
                        @CStoryComponent
                        @Composable
                        fun Primary(onClick: () -> Unit) {}
                    }
                }
                """.trimIndent(),
            )
        projectDir.resolve("design-system/stories/build.gradle.kts").apply { parentFile.mkdirs() }.writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform")
                id("org.jetbrains.compose")
                id("org.jetbrains.kotlin.plugin.compose")
                id("dev.cstories.gradle")
            }

            @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
            kotlin {
                wasmJs {
                    browser()
                    binaries.executable()
                }
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":design-system"))
                    }
                }
            }
            """.trimIndent(),
        )
        projectDir.resolve("design-system/stories/src/commonMain/kotlin/stories/SynButtonStories.kt")
            .apply { parentFile.mkdirs() }
            .writeText(
                """
                package stories

                import androidx.compose.runtime.Composable
                import designSystem.SynButton
                import io.cstories.annotations.CStory
                import io.cstories.generated.CStoryComponentRefs

                @CStory(collection = "Test", group = "Buttons", name = "Primary", component = CStoryComponentRefs.SynButton.Primary)
                @Composable
                fun PrimaryStory() {
                    SynButton.Primary(onClick = {})
                }
                """.trimIndent(),
            )
    }

    private fun requiredProperty(name: String): String =
        checkNotNull(System.getProperty(name)) { "Missing system property '$name', set by the test task" }
}
