package io.cstories.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertTrue

class HomeMarkdownGenerationTest {
    @Test
    fun `registry exposes a null home markdown when CSTORIES md is missing`() {
        val registry = generateRegistry(homeMarkdown = null)

        assertTrue("val homeMarkdown: String? = null" in registry, registry)
    }

    @Test
    fun `registry embeds CSTORIES md as an escaped Kotlin string`() {
        val markdown = "# Lumen\n\nPrice: \$10, path C:\\lib, \"quoted\" and \"\"\"triple\"\"\""

        val registry = generateRegistry(homeMarkdown = markdown)

        val expectedLiteral = "\"# Lumen\\n\\nPrice: \\\$10, path C:\\\\lib, \\\"quoted\\\" and " +
            "\\\"\\\"\\\"triple\\\"\\\"\\\"\""
        assertTrue("val homeMarkdown: String? = listOf($expectedLiteral).joinToString(\"\")" in registry, registry)
    }

    @Test
    fun `long home markdown is split into several literals`() {
        val registry = generateRegistry(homeMarkdown = "a".repeat(20_000))

        val literalCount = Regex("\"a+\"").findAll(registry).count()
        assertTrue(literalCount == 3, "expected 3 literals, found $literalCount")
    }

    @Test
    fun `desktop entry point passes the home markdown to the app`() {
        val project = ProjectBuilder.builder().build()
        val task = registerTask(project, homeMarkdown = null)

        task.actions.forEach { it.execute(task) }

        val entryPoint = project.layout.buildDirectory
            .file("desktop/io/cstories/generated/CStoriesDesktopEntryPoint.kt").get().asFile.readText()
        assertTrue("homeMarkdown = AllStoriesRegistry.homeMarkdown" in entryPoint, entryPoint)
    }

    private fun generateRegistry(homeMarkdown: String?): String {
        val project = ProjectBuilder.builder().build()
        val task = registerTask(project, homeMarkdown)

        task.actions.forEach { it.execute(task) }

        return project.layout.buildDirectory
            .file("desktop/io/cstories/generated/AllStoriesRegistry.kt").get().asFile.readText()
    }

    private fun registerTask(project: org.gradle.api.Project, homeMarkdown: String?): CStoriesAggregateTask {
        val markdownFile = project.layout.projectDirectory.file("CSTORIES.md").asFile
        if (homeMarkdown != null) markdownFile.writeText(homeMarkdown)
        return project.tasks.register("aggregate", CStoriesAggregateTask::class.java) {
            packageName.set("io.cstories.generated")
            applicationName.set("Test")
            generateWasmJsEntryPoint.set(false)
            generateDesktopEntryPoint.set(true)
            desktopOutputDirectory.set(project.layout.buildDirectory.dir("desktop"))
            homeMarkdownFile.from(markdownFile)
        }.get()
    }
}
