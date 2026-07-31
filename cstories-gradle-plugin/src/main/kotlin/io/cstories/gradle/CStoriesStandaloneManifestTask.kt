package io.cstories.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Lists every file that makes up a servable wasmJs site — possibly spread
 * across several directories (e.g. the resources pipeline output and the
 * webpack bundle output aren't the same directory in dev mode) — and writes
 * that combined list as a JSON manifest into [manifestOutputDirectory].
 *
 * The catalog's Export button reads this manifest at runtime to know
 * exactly which files to fetch and zip client-side into a standalone,
 * downloadable copy of the site — no server-side zipping involved.
 *
 * [manifestOutputDirectory] must be a directory actually served over HTTP
 * by whatever serves the site (the production `dist` folder, or the
 * directory the dev server serves static files from), otherwise the Export
 * button won't be able to fetch the manifest.
 */
abstract class CStoriesStandaloneManifestTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val siteDirectories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val manifestOutputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val files = siteDirectories.files
            .filter { it.isDirectory }
            .flatMap { dir ->
                dir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.relativeTo(dir).invariantSeparatorsPath }
                    .toList()
            }
            .filter { it != MANIFEST_FILE_NAME }
            .distinct()
            .sorted()

        val outputDir = manifestOutputDirectory.get().asFile.apply { mkdirs() }
        File(outputDir, MANIFEST_FILE_NAME).writeText(buildManifestJson(files))
    }

    private fun buildManifestJson(files: List<String>): String {
        val filesJson = files.joinToString(separator = ",\n") { "    \"${it.escapeJson()}\"" }
        return """
            |{
            |  "files": [
            |$filesJson
            |  ]
            |}
        """.trimMargin()
    }

    private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        const val MANIFEST_FILE_NAME = "cstories-manifest.json"
    }
}
