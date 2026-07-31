package io.cstories.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class CStoriesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return CStoriesProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            moduleName = environment.options[MODULE_NAME_OPTION]
                ?: System.getProperty(MODULE_NAME_OPTION).orEmpty(),
        )
    }
}

const val MODULE_NAME_OPTION = "cstories.moduleName"
