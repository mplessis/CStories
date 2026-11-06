package io.cstories.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class WebResourcesGenerationTest {
    @Test
    fun `wasm web resources include the favicon linked from index html`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("aggregate", CStoriesAggregateTask::class.java) {
            packageName.set("io.cstories.generated")
            applicationName.set("Test")
            jsBundleBaseName.set("sample")
            generateWasmJsEntryPoint.set(true)
            generateDesktopEntryPoint.set(false)
            wasmJsOutputDirectory.set(project.layout.buildDirectory.dir("wasmJs"))
            webResourcesDirectory.set(project.layout.buildDirectory.dir("web"))
        }.get()

        task.actions.forEach { it.execute(task) }

        val webDir = project.layout.buildDirectory.dir("web").get().asFile
        val indexHtml = webDir.resolve("index.html").readText()
        assertTrue("""<link rel="icon" type="image/png" href="favicon.png">""" in indexHtml, indexHtml)
        val bundledFavicon = CStoriesAggregateTask::class.java.getResourceAsStream("favicon.png")!!.use { it.readBytes() }
        assertContentEquals(bundledFavicon, webDir.resolve("favicon.png").readBytes())
    }
}
