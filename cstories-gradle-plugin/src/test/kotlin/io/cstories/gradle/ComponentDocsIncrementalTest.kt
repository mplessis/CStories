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
    @Test
    fun `editing only a component KDoc regenerates documentation in the stories module`() {
        val projectDir = Files.createTempDirectory("cstories-docs-incremental").toFile()
        try {
            val project = ComponentsConsumerProject(projectDir)
            project.write("docs-incremental-consumer")

            runBuild(projectDir)
            project.assertRefsContain("Original alpha docs.")

            project.writeComponentDocumentation("Edited alpha docs.")

            runBuild(projectDir)
            project.assertRefsContain("Edited alpha docs.")

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
            .withArguments(":lib:stories:compileKotlinJvm", "--no-configuration-cache", "--stacktrace")
            .forwardOutput()
            .build()
}
