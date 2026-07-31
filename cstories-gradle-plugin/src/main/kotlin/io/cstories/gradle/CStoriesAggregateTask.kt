package io.cstories.gradle

import org.gradle.api.DefaultTask
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
            .flatMap(::readRegistries)
            .distinct()
            .sorted()

        val packagePath = packageName.get().replace('.', '/')
        val registrySource = buildRegistrySource(registries)

        if (generateWasmJsEntryPoint.get()) {
            val wasmJsKotlinDir = File(wasmJsOutputDirectory.get().asFile, packagePath).apply { mkdirs() }
            File(wasmJsKotlinDir, "AllStoriesRegistry.kt").writeText(registrySource)
            File(wasmJsKotlinDir, "CStoriesWasmJsEntryPoint.kt").writeText(buildWasmJsEntryPointSource())

            val webResourcesDir = webResourcesDirectory.get().asFile.apply { mkdirs() }
            File(webResourcesDir, "index.html").writeText(buildIndexHtmlSource())
        }

        if (generateDesktopEntryPoint.get()) {
            val desktopKotlinDir = File(desktopOutputDirectory.get().asFile, packagePath).apply { mkdirs() }
            File(desktopKotlinDir, "AllStoriesRegistry.kt").writeText(registrySource)
            File(desktopKotlinDir, "CStoriesDesktopEntryPoint.kt").writeText(buildDesktopEntryPointSource())
        }
    }

    private fun readRegistries(file: File): List<String> {
        return when {
            file.isDirectory -> readRegistriesFromDirectory(file)
            file.extension == "jar" -> readRegistriesFromJar(file)
            else -> emptyList()
        }
    }

    private fun readRegistriesFromDirectory(directory: File): List<String> {
        val manifest = File(directory, MANIFEST_PATH)
        if (!manifest.exists()) {
            return emptyList()
        }
        return manifest.readLines().map(String::trim).filter(String::isNotBlank)
    }

    private fun readRegistriesFromJar(file: File): List<String> {
        JarFile(file).use { jar ->
            val entry = jar.getJarEntry(MANIFEST_PATH) ?: return emptyList()
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

    private fun buildWasmJsEntryPointSource(): String {
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.ExperimentalComposeUiApi
            |import androidx.compose.ui.window.CanvasBasedWindow
            |import io.cstories.runtime.CStoriesApp
            |
            |@OptIn(ExperimentalComposeUiApi::class)
            |fun main() {
            |    CanvasBasedWindow("CStories") {
            |        CStoriesApp(AllStoriesRegistry.entries)
            |    }
            |}
        """.trimMargin()
    }

    private fun buildDesktopEntryPointSource(): String {
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.window.singleWindowApplication
            |import io.cstories.runtime.CStoriesApp
            |
            |fun main() {
            |    singleWindowApplication(title = "CStories") {
            |        CStoriesApp(AllStoriesRegistry.entries)
            |    }
            |}
        """.trimMargin()
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
        const val MANIFEST_PATH = "META-INF/cstories/registries.txt"
    }
}
