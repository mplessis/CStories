package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CStoriesGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("org.jetbrains.compose")
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = requireNotNull(project.extensions.findByType<KotlinMultiplatformExtension>()) {
            "CStories requires the Kotlin Multiplatform plugin"
        }

        configureKotlin(project, kotlin)
        configureDependencies(project)
        configureKsp(project)
        wireKspCommonMainSources(project, kotlin)
        val aggregateTask = registerAggregateTask(project, kotlin)
        wireGeneratedSources(project, kotlin, aggregateTask)
        registerRunAlias(project)
    }

    @OptIn(ExperimentalWasmDsl::class)
    private fun configureKotlin(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
    ) {
        val wasmTarget = kotlin.targets.findByName("wasmJs")
        if (wasmTarget == null) {
            kotlin.wasmJs {
                browser()
                binaries.executable()
            }
        } else {
            val wasmCompilations = kotlin.wasmJs()
            wasmCompilations.browser()
            wasmCompilations.binaries.executable()
        }
    }

    private fun configureDependencies(project: Project) {
        project.dependencies {
            add("commonMainImplementation", project.project(":cstories-annotations"))
            add("commonMainImplementation", project.project(":cstories-runtime"))
            add("kspCommonMainMetadata", project.project(":cstories-processor"))
        }
    }

    private fun configureKsp(project: Project) {
        project.extensions.findByType(KspExtension::class.java)?.apply {
            arg(MODULE_NAME_OPTION, project.name)
        }
    }

    /**
     * KSP writes generated commonMain code into
     * `build/generated/ksp/metadata/commonMain/kotlin`, but does not register
     * that directory as a commonMain source directory automatically. Without
     * this, generated declarations (like the per-module story registry) are
     * invisible to every platform compilation (jvm, wasmJs, ...).
     */
    private fun wireKspCommonMainSources(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
    ) {
        val kspCommonMainKotlinDir = project.layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin")
        kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(kspCommonMainKotlinDir)

        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }

    private fun registerAggregateTask(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
    ): TaskProvider<CStoriesAggregateTask> {
        return project.tasks.register<CStoriesAggregateTask>("cstoriesAggregateRegistries") {
            packageName.set("io.cstories.generated")
            jsBundleBaseName.set(project.name)
            outputDirectory.set(project.layout.buildDirectory.dir("generated/cstories/wasmJsMain/kotlin"))
            webResourcesDirectory.set(project.layout.buildDirectory.dir("generated/cstories/wasmJsMain/resources"))
            runtimeClasspath.from(project.configurations.getByName("wasmJsRuntimeClasspath"))
            // KSP writes the manifest for this module's own stories into the
            // commonMain metadata resources output, which is not part of the
            // wasmJs runtime classpath. Scan it explicitly so local stories
            // are always picked up, in addition to any transitive dependency
            // jars already published with the manifest bundled in.
            runtimeClasspath.from(project.layout.buildDirectory.dir("generated/ksp/metadata/commonMain/resources"))
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }

    private fun wireGeneratedSources(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        aggregateTask: TaskProvider<CStoriesAggregateTask>,
    ) {
        val wasmJsMain = kotlin.sourceSets.getByName("wasmJsMain")
        wasmJsMain.kotlin.srcDir(aggregateTask.map { it.outputDirectory })
        wasmJsMain.resources.srcDir(aggregateTask.map { it.webResourcesDirectory })
        project.tasks.matching { it.name == "compileKotlinWasmJs" }.configureEach {
            dependsOn(aggregateTask)
        }
        project.tasks.matching { it.name == "wasmJsProcessResources" }.configureEach {
            dependsOn(aggregateTask)
        }
    }

    private fun registerRunAlias(project: Project) {
        project.tasks.register("runCStories") {
            group = "cstories"
            description = "Runs the generated CStories catalog"
            dependsOn("wasmJsBrowserDevelopmentRun")
        }
    }
}

private const val MODULE_NAME_OPTION = "cstories.moduleName"
