package io.cstories.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * End-to-end check that a KDoc-only edit in a components module reaches the
 * `CStoryComponentRefs` generated in a dependent stories module, even though
 * such an edit never changes the components module's ABI.
 */
class ComponentDocsIncrementalTest {
    private val repoRoot = File(requiredProperty("cstories.repoRoot"))
    private val kotlinVersion = requiredProperty("cstories.kotlinVersion")
    private val composeVersion = requiredProperty("cstories.composeVersion")

    @Test
    fun `editing only a component KDoc regenerates documentation in the stories module`() {
        val projectDir = Files.createTempDirectory("cstories-docs-incremental").toFile()
        try {
            writeConsumerProject(projectDir)

            runBuild(projectDir)
            assertRefsContain(projectDir, "Original alpha docs.")

            componentFile(projectDir).writeText(componentSource("Edited alpha docs."))

            runBuild(projectDir)
            assertRefsContain(projectDir, "Edited alpha docs.")

            val upToDate = runBuild(projectDir)
            listOf(":lib:stories:cstoriesExtractComponentMetadata", ":lib:stories:kspKotlinJvm").forEach { path ->
                val outcome = upToDate.task(path)?.outcome
                assertTrue(
                    outcome == TaskOutcome.UP_TO_DATE || outcome == TaskOutcome.FROM_CACHE,
                    "$path should be up-to-date on a no-change build, was $outcome",
                )
            }
        } finally {
            projectDir.deleteRecursively()
        }
    }

    private fun runBuild(projectDir: File): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(":lib:stories:compileKotlinJvm", "--stacktrace")
            .forwardOutput()
            .build()

    private fun assertRefsContain(projectDir: File, documentation: String) {
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

    private fun componentFile(projectDir: File) = projectDir.resolve("lib/src/commonMain/kotlin/demo/Alpha.kt")

    private fun componentSource(documentation: String) =
        """
        package demo

        import androidx.compose.runtime.Composable
        import io.cstories.annotations.CStoryComponent

        /** $documentation */
        @CStoryComponent
        @Composable
        fun Alpha() {}
        """.trimIndent()

    private fun writeConsumerProject(projectDir: File) {
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
            rootProject.name = "docs-incremental-consumer"
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
        componentFile(projectDir).apply { parentFile.mkdirs() }.writeText(componentSource("Original alpha docs."))

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
