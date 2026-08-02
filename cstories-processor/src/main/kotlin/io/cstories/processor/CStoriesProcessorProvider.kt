package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

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
        )
    }
}

const val MODULE_NAME_OPTION = "cstories.moduleName"
const val PROCESS_MODE_OPTION = "cstories.processMode"
