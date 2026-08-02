package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStreamWriter

/** Writes the `META-INF/cstories/theme-wrapper.txt` manifest entry for a single `@CStoryThemeWrapper` property. */
internal object ThemeWrapperManifestWriter {
    fun write(codeGenerator: CodeGenerator, propertyFqn: String) {
        codeGenerator
            .createNewFile(
                // See `StoryRegistryGenerator` for why this uses
                // `Dependencies.ALL_FILES` rather than
                // `Dependencies(aggregating = true)`.
                dependencies = Dependencies.ALL_FILES,
                packageName = "",
                fileName = "META-INF/cstories/theme-wrapper",
                extensionName = "txt",
            )
            .use { output ->
                OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                    writer.write(propertyFqn)
                    writer.write("\n")
                }
            }
    }
}
