package io.cstories.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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

    @get:Input
    abstract val applicationName: Property<String>

    @get:Optional
    @get:Input
    abstract val jsBundleBaseName: Property<String>

    @get:Input
    abstract val generateWasmJsEntryPoint: Property<Boolean>

    @get:Input
    abstract val generateDesktopEntryPoint: Property<Boolean>

    /**
     * Candidate location of the optional `CSTORIES.md` home page introduction.
     * Declared as a file collection so that the file may be missing, and that
     * creating, editing or deleting it reruns this task.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val homeMarkdownFile: ConfigurableFileCollection

    @TaskAction
    fun generate() {
        val registries = runtimeClasspath.files
            .flatMap { readManifest(it, REGISTRIES_MANIFEST_PATH) }
            .distinct()
            .sorted()

        val themeWrapperCandidates = runtimeClasspath.files
            .flatMap { readManifest(it, THEME_WRAPPER_MANIFEST_PATH) }
            .distinct()
        if (themeWrapperCandidates.size > 1) {
            throw org.gradle.api.GradleException(
                "Only one CustomCStoriesThemeWrapper is allowed across the whole project, found " +
                    themeWrapperCandidates.joinToString(),
            )
        }
        val themeWrapperReference = themeWrapperCandidates.singleOrNull()

        val packagePath = packageName.get().replace('.', '/')
        val homeMarkdown = homeMarkdownFile.files.firstOrNull(File::isFile)?.readText()
        val registrySource = buildRegistrySource(registries, homeMarkdown)

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

    private fun buildRegistrySource(registries: List<String>, homeMarkdown: String?): String {
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
            |
            |    val homeMarkdown: String? = ${homeMarkdownInitializer(homeMarkdown)}
            |}
        """.trimMargin()
    }

    /**
     * Kotlin expression for the home page Markdown. The text is split into
     * several string literals joined at runtime, because a single JVM string
     * constant is limited to 65535 bytes.
     */
    private fun homeMarkdownInitializer(homeMarkdown: String?): String {
        if (homeMarkdown == null) return "null"
        val literals = homeMarkdown
            .chunked(HOME_MARKDOWN_CHUNK_SIZE)
            .joinToString(", ") { chunk -> "\"${chunk.escapeKotlinString()}\"" }
        return "listOf($literals).joinToString(\"\")"
    }

    private fun buildWasmJsEntryPointSource(themeWrapperReference: String?): String {
        val themeWrapperImport = themeWrapperReference?.let { "\nimport $it" } ?: ""
        val themeWrapperArg = themeWrapperArgument(themeWrapperReference)
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.ExperimentalComposeUiApi
            |import androidx.compose.ui.window.ComposeViewport
            |import io.cstories.runtime.CStoriesApp
            |import kotlinx.browser.document$themeWrapperImport
            |
            |@OptIn(ExperimentalComposeUiApi::class)
            |fun main() {
            |    ComposeViewport(document.body!!) {
            |        CStoriesApp(AllStoriesRegistry.entries$themeWrapperArg, homeMarkdown = AllStoriesRegistry.homeMarkdown)
            |    }
            |}
        """.trimMargin()
    }

    private fun buildDesktopEntryPointSource(themeWrapperReference: String?): String {
        val themeWrapperImport = themeWrapperReference?.let { "\nimport $it" } ?: ""
        val themeWrapperArg = themeWrapperArgument(themeWrapperReference)
        val escapedApplicationName = applicationName.get().escapeKotlinString()
        return """
            |package ${packageName.get()}
            |
            |import androidx.compose.ui.unit.DpSize
            |import androidx.compose.ui.unit.dp
            |import androidx.compose.ui.window.Window
            |import androidx.compose.ui.window.WindowState
            |import androidx.compose.ui.window.application
            |import io.cstories.runtime.CStoriesApp
            |import io.cstories.runtime.cstoriesDesktopWindowIcon
            |import io.cstories.runtime.configureDesktopAppIcon
            |import java.awt.Dimension$themeWrapperImport
            |
            |private val MinWindowSize = DpSize(1600.dp, 1200.dp)
            |
            |fun main() {
            |    System.setProperty("apple.awt.application.name", "$escapedApplicationName")
            |    configureDesktopAppIcon()
            |    application {
            |        Window(
            |            onCloseRequest = ::exitApplication,
            |            title = "$escapedApplicationName",
            |            state = WindowState(size = MinWindowSize),
            |            icon = cstoriesDesktopWindowIcon(),
            |        ) {
            |            window.minimumSize = Dimension(1600, 1200)
            |            CStoriesApp(AllStoriesRegistry.entries$themeWrapperArg, homeMarkdown = AllStoriesRegistry.homeMarkdown)
            |        }
            |    }
            |}
        """.trimMargin()
    }

    /** Trailing `, themeWrapper = <SimpleName>` constructor argument, or an empty string when unset. */
    private fun themeWrapperArgument(themeWrapperReference: String?): String {
        val simpleName = themeWrapperReference?.substringAfterLast('.') ?: return ""
        return ", themeWrapper = $simpleName"
    }

    private fun String.escapeKotlinString(): String =
        replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("$", "\\$")

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    private fun buildIndexHtmlSource(): String {
        val escapedApplicationName = applicationName.get().escapeHtml()
        return """
            |<!DOCTYPE html>
            |<html lang="en">
            |<head>
            |    <meta charset="UTF-8">
            |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
            |    <title>$escapedApplicationName</title>
            |    <style>
            |        html, body {
            |            margin: 0;
            |            padding: 0;
            |            width: 100%;
            |            height: 100%;
            |        }
            |    </style>
            |</head>
            |<body>
            |    <script src="${jsBundleBaseName.get()}.js"></script>
            |</body>
            |</html>
        """.trimMargin()
    }

    private companion object {
        const val REGISTRIES_MANIFEST_PATH = "META-INF/cstories/registries.txt"
        const val THEME_WRAPPER_MANIFEST_PATH = "META-INF/cstories/theme-wrapper.txt"
        const val HOME_MARKDOWN_CHUNK_SIZE = 8_000
    }
}
