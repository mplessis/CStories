package io.cstories.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.jar.JarFile

abstract class CStoriesAggregateTask : DefaultTask() {
    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Optional
    @get:OutputDirectory
    abstract val wasmJsOutputDirectory: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val desktopOutputDirectory: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val webResourcesDirectory: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Optional
    @get:Input
    abstract val jsBundleBaseName: Property<String>

    @get:Input
    abstract val generateWasmJsEntryPoint: Property<Boolean>

    @get:Input
    abstract val generateDesktopEntryPoint: Property<Boolean>

    @TaskAction
    fun generate() {
        val registries = runtimeClasspath.files
            .flatMap { readManifest(it, REGISTRIES_MANIFEST_PATH) }
            .distinct()
            .sorted()

        // A `@CStoryThemeWrapper` property is picked up by `cstories-processor`
        // (see `CStoriesProcessor.runThemeWrapperPass`) wherever it's declared
        // in the dependency graph, so at most one manifest entry should ever
        // be found across the whole aggregated classpath.
        val themeWrapperCandidates = runtimeClasspath.files
            .flatMap { readManifest(it, THEME_WRAPPER_MANIFEST_PATH) }
            .distinct()
        if (themeWrapperCandidates.size > 1) {
            throw GradleException(
                "Only one @CStoryThemeWrapper property is allowed across the whole project, found " +
                    "${themeWrapperCandidates.size}: ${themeWrapperCandidates.joinToString()}",
            )
        }
        val themeWrapperReference = themeWrapperCandidates.singleOrNull()

        val packagePath = packageName.get().replace('.', '/')
        val registrySource = buildRegistrySource(registries)

        if (generateWasmJsEntryPoint.get()) {
            val wasmJsKotlinDir = File(wasmJsOutputDirectory.get().asFile, packagePath).apply { mkdirs() }
            File(wasmJsKotlinDir, "AllStoriesRegistry.kt").writeText(registrySource)
            File(wasmJsKotlinDir, "CStoriesWasmJsEntryPoint.kt")
                .writeText(buildWasmJsEntryPointSource(themeWrapperReference))

            val webResourcesDir = webResourcesDirectory.get().asFile.apply { mkdirs() }
            File(webResourcesDir, "index.html").writeText(buildIndexHtmlSource())
        }

        if (generateDesktopEntryPoint.get()) {
            val desktopKotlinDir = File(desktopOutputDirectory.get().asFile, packagePath).apply { mkdirs() }
            File(desktopKotlinDir, "AllStoriesRegistry.kt").writeText(registrySource)
            File(desktopKotlinDir, "CStoriesDesktopEntryPoint.kt")
                .writeText(buildDesktopEntryPointSource(themeWrapperReference))
        }
    }

    private fun readManifest(file: File, manifestPath: String): List<String> {
        return when {
            file.isDirectory -> readManifestFromDirectory(file, manifestPath)
            file.extension == "jar" -> readManifestFromJar(file, manifestPath)
            else -> emptyList()
        }
    }

    private fun readManifestFromDirectory(directory: File, manifestPath: String): List<String> {
        val manifest = File(directory, manifestPath)
        if (!manifest.exists()) {
            return emptyList()
        }
        return manifest.readLines().map(String::trim).filter(String::isNotBlank)
    }

    private fun readManifestFromJar(file: File, manifestPath: String): List<String> {
        JarFile(file).use { jar ->
            val entry = jar.getJarEntry(manifestPath) ?: return emptyList()
            return jar.getInputStream(entry)
                .bufferedReader()
                .readLines()
                .map(String::trim)
                .filter(String::isNotBlank)
        }
    }

    private fun buildRegistrySource(registries: List<String>): String {
        val entriesInitializer = if (registries.isEmpty()) {
            "emptyList()"
        } else {
            registries.joinToString(separator = " +\n        ") { "$it.entries" }
        }

        return """
            |package ${packageName.get()}
            |
            |import io.cstories.runtime.StoryEntry
            |
            |object AllStoriesRegistry {
            |    val entries: List<StoryEntry> =
            |        $entriesInitializer
            |}
        """.trimMargin()
    }

    private fun buildWasmJsEntryPointSource(themeWrapperReference: String?): String {
        val themeWrapperImport = themeWrapperReference?.let { "\nimport $it" } ?: ""
        val themeWrapperArg = themeWrapperArgument(themeWrapperReference)
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.ExperimentalComposeUiApi
            |import androidx.compose.ui.window.CanvasBasedWindow
            |import io.cstories.runtime.CStoriesApp$themeWrapperImport
            |
            |@OptIn(ExperimentalComposeUiApi::class)
            |fun main() {
            |    CanvasBasedWindow("CStories") {
            |        CStoriesApp(AllStoriesRegistry.entries$themeWrapperArg)
            |    }
            |}
        """.trimMargin()
    }

    private fun buildDesktopEntryPointSource(themeWrapperReference: String?): String {
        val themeWrapperImport = themeWrapperReference?.let { "\nimport $it" } ?: ""
        val themeWrapperArg = themeWrapperArgument(themeWrapperReference)
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.unit.DpSize
            |import androidx.compose.ui.unit.dp
            |import androidx.compose.ui.window.WindowState
            |import androidx.compose.ui.window.singleWindowApplication
            |import io.cstories.runtime.CStoriesApp
            |import java.awt.Dimension$themeWrapperImport
            |
            |private val MinWindowSize = DpSize(1600.dp, 1200.dp)
            |
            |fun main() {
            |    singleWindowApplication(
            |        title = "CStories",
            |        state = WindowState(size = MinWindowSize),
            |    ) {
            |        window.minimumSize = Dimension(1600, 1200)
            |        CStoriesApp(AllStoriesRegistry.entries$themeWrapperArg)
            |    }
            |}
        """.trimMargin()
    }

    /** Trailing `, themeWrapper = <SimpleName>` constructor argument, or an empty string when unset. */
    private fun themeWrapperArgument(themeWrapperReference: String?): String {
        val simpleName = themeWrapperReference?.substringAfterLast('.') ?: return ""
        return ", themeWrapper = $simpleName"
    }

    private fun buildIndexHtmlSource(): String {
        return """
            |<!DOCTYPE html>
            |<html lang="en">
            |<head>
            |    <meta charset="UTF-8">
            |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
            |    <title>CStories</title>
            |    <style>
            |        html, body {
            |            margin: 0;
            |            padding: 0;
            |            width: 100%;
            |            height: 100%;
            |        }
            |
            |        #ComposeTarget {
            |            width: 100%;
            |            height: 100%;
            |            display: block;
            |        }
            |    </style>
            |</head>
            |<body>
            |    <canvas id="ComposeTarget"></canvas>
            |    <script src="${jsBundleBaseName.get()}.js"></script>
            |</body>
            |</html>
        """.trimMargin()
    }

    private companion object {
        const val REGISTRIES_MANIFEST_PATH = "META-INF/cstories/registries.txt"
        const val THEME_WRAPPER_MANIFEST_PATH = "META-INF/cstories/theme-wrapper.txt"
    }
}
