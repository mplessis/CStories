package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import java.io.File

class CStoriesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val mode = environment.options[PROCESS_MODE_OPTION]
        return CStoriesProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            moduleName = environment.options[MODULE_NAME_OPTION]
                ?: System.getProperty(MODULE_NAME_OPTION).orEmpty(),
            processComponents = mode == "common" || mode == "standalone",
            processStories = mode != "common",
            componentMetadata = environment.options[COMPONENT_METADATA_OPTION]
                ?.let(::File)
                ?.takeIf(File::exists)
                ?.readLines()
                ?.let(ComponentRefsGenerator::decodeMetadata)
                .orEmpty(),
            writeComponentMetadata = environment.options[WRITE_COMPONENT_METADATA_OPTION] == "true",
        )
    }
}

const val MODULE_NAME_OPTION = "cstories.moduleName"
const val PROCESS_MODE_OPTION = "cstories.processMode"
const val COMPONENT_METADATA_OPTION = "cstories.componentMetadata"
const val WRITE_COMPONENT_METADATA_OPTION = "cstories.writeComponentMetadata"
