package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStreamWriter

internal object RegistryManifestWriter {
    fun write(codeGenerator: CodeGenerator, registry: GeneratedRegistry) {
        codeGenerator
            .createNewFile(
                // See `StoryRegistryGenerator` for why this uses
                // `Dependencies.ALL_FILES` rather than
                // `Dependencies(aggregating = true)`.
                dependencies = Dependencies.ALL_FILES,
                packageName = "",
                fileName = "META-INF/cstories/registries",
                extensionName = "txt",
            )
            .use { output ->
                OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                    writer.write(registry.qualifiedName)
                    writer.write("\n")
                }
            }
    }
}
