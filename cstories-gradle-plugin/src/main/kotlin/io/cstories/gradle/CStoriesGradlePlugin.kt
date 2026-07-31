package io.cstories.gradle
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.JavaForkOptions
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
        project.extensions.extraProperties["ksp.allow.all.target.configuration"] = false
        project.dependencies {
            add("kspCommonMainMetadata", mapOf("path" to ":cstories-processor"))
        }
        project.tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("KotlinMetadata") }.configureEach {
            doFirst {
                (this as? JavaForkOptions)?.systemProperty(MODULE_NAME_OPTION, project.name)
            }
        }
    }

    private fun registerAggregateTask(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
    ): TaskProvider<CStoriesAggregateTask> {
        return project.tasks.register<CStoriesAggregateTask>("cstoriesAggregateRegistries") {
            packageName.set("io.cstories.generated")
            outputDirectory.set(project.layout.buildDirectory.dir("generated/cstories/wasmJsMain/kotlin"))
            runtimeClasspath.from(project.configurations.getByName("wasmJsRuntimeClasspath"))
        }
    }

    private fun wireGeneratedSources(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        aggregateTask: TaskProvider<CStoriesAggregateTask>,
    ) {
        kotlin.sourceSets.getByName("wasmJsMain").kotlin.srcDir(aggregateTask.map { it.outputDirectory })
        project.tasks.matching { it.name == "compileKotlinWasmJs" }.configureEach {
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
