package io.cstories.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * End-to-end check that a stories module consuming a components module
 * (both on `jvm` and `wasmJs`) builds with the Gradle configuration cache,
 * reuses the cache entry, and still regenerates component docs on change.
 */
class ConfigurationCacheTest {
    @Test
    fun `stories module builds and reuses the configuration cache`() {
        val projectDir = Files.createTempDirectory("cstories-configuration-cache").toFile()
        try {
            val project = ComponentsConsumerProject(projectDir)
            project.write("configuration-cache-consumer")

            val first = runBuild(projectDir)
            assertTrue("Configuration cache entry stored" in first.output, "First build should store a cache entry")
            project.assertRefsContain("Original alpha docs.")

            val second = runBuild(projectDir)
            assertTrue("Configuration cache entry reused" in second.output, "Second build should reuse the cache entry")

            // The first build adds the aggregated registry sources to the KSP
            // inputs, so KSP only settles from the second build onwards.
            val third = runBuild(projectDir)
            assertTrue("Configuration cache entry reused" in third.output, "Third build should reuse the cache entry")
            val kspOutcome = third.task(":lib:stories:kspKotlinJvm")?.outcome
            assertTrue(
                kspOutcome == TaskOutcome.UP_TO_DATE || kspOutcome == TaskOutcome.FROM_CACHE,
                "kspKotlinJvm should be up-to-date on a no-change build, was $kspOutcome",
            )

            project.writeComponentDocumentation("Edited alpha docs.")

            val edited = runBuild(projectDir)
            assertTrue("Configuration cache entry reused" in edited.output, "A source edit should not invalidate the cache")
            project.assertRefsContain("Edited alpha docs.")
        } finally {
            projectDir.deleteRecursively()
        }
    }

    private fun runBuild(projectDir: File): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":lib:stories:compileKotlinJvm",
                ":lib:stories:compileKotlinWasmJs",
                "--configuration-cache",
                "--stacktrace",
            )
            .forwardOutput()
            .build()
}
