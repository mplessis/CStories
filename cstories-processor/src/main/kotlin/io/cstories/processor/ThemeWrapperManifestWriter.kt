package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStreamWriter

internal object ThemeWrapperManifestWriter {
    private const val PATH = "META-INF/cstories/theme-wrapper.txt"

    fun write(codeGenerator: CodeGenerator, reference: String) {
        codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "",
            fileName = PATH.removeSuffix(".txt"),
            extensionName = "txt",
        ).use { output ->
            OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                writer.write(reference)
                writer.write("\n")
            }
        }
    }
}
