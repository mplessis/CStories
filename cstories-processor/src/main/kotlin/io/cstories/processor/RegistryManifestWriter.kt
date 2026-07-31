package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import java.io.OutputStreamWriter

internal object RegistryManifestWriter {
    fun write(codeGenerator: CodeGenerator, registry: GeneratedRegistry, files: List<KSFile>) {
        codeGenerator
            .createNewFile(
                dependencies = Dependencies(aggregating = false, sources = files.toTypedArray()),
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
