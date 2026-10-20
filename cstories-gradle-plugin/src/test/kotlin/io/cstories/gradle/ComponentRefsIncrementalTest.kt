package io.cstories.gradle

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * End-to-end check that KSP's incremental processing keeps every
 * `@CStoryComponent` in the generated `CStoryComponentRefs` object, even when
 * only one component source file changed since the previous build.
 */
class ComponentRefsIncrementalTest {
    private val repoRoot = File(requiredProperty("cstories.repoRoot"))
    private val kotlinVersion = requiredProperty("cstories.kotlinVersion")
    private val composeVersion = requiredProperty("cstories.composeVersion")

    @Test
    fun `editing one component keeps every reference after an incremental build`() {
        val projectDir = Files.createTempDirectory("cstories-incremental").toFile()
        try {
            writeConsumerProject(projectDir)

            runBuild(projectDir)
            assertRefsContain(projectDir, "Alpha", "Beta")

            projectDir.resolve("src/commonMain/kotlin/demo/Alpha.kt").appendText("\n// edited\n")

            runBuild(projectDir)
            assertRefsContain(projectDir, "Alpha", "Beta")
        } finally {
            projectDir.deleteRecursively()
        }
    }

    private fun runBuild(projectDir: File) {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("compileKotlinJvm", "--stacktrace")
            .forwardOutput()
            .build()
    }

    private fun assertRefsContain(projectDir: File, vararg names: String) {
        val refs = projectDir
            .resolve("build/generated/ksp/metadata/commonMain/kotlin/io/cstories/generated/CStoryComponentRefs.kt")
            .readText()
        names.forEach { name ->
            assertTrue("const val $name:" in refs, "CStoryComponentRefs is missing $name:\n$refs")
        }
    }

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
            rootProject.name = "incremental-consumer"
            """.trimIndent(),
        )
        projectDir.resolve("gradle.properties").writeText("org.gradle.jvmargs=-Xmx2g\nkotlin.daemon.jvmargs=-Xmx2g\n")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform") version "$kotlinVersion"
                id("org.jetbrains.compose") version "$composeVersion"
                id("org.jetbrains.kotlin.plugin.compose") version "$kotlinVersion"
                id("dev.cstories.gradle")
            }

            @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
            kotlin {
                jvm()
                wasmJs {
                    browser()
                    binaries.executable()
                }
            }
            """.trimIndent(),
        )

        val sources = projectDir.resolve("src/commonMain/kotlin/demo").apply { mkdirs() }
        listOf("Alpha", "Beta").forEach { name ->
            sources.resolve("$name.kt").writeText(
                """
                package demo

                import androidx.compose.runtime.Composable
                import io.cstories.annotations.CStoryComponent

                /** The $name component. */
                @CStoryComponent
                @Composable
                fun $name() {}
                """.trimIndent(),
            )
        }
        sources.resolve("Stories.kt").writeText(
            """
            package demo

            import androidx.compose.runtime.Composable
            import io.cstories.annotations.CStory
            import io.cstories.generated.CStoryComponentRefs

            @CStory(collection = "Test", group = "Components", name = "Alpha", component = CStoryComponentRefs.Alpha)
            @Composable
            fun AlphaStory() {
                Alpha()
            }

            @CStory(collection = "Test", group = "Components", name = "Beta", component = CStoryComponentRefs.Beta)
            @Composable
            fun BetaStory() {
                Beta()
            }
            """.trimIndent(),
        )
    }

    private fun requiredProperty(name: String): String =
        checkNotNull(System.getProperty(name)) { "Missing system property '$name', set by the test task" }
}
