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
        registerStandaloneManifestTask(project)
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

    /**
     * Prefers a sibling subproject (`:cstories-annotations`, ...) when this
     * build actually has one — which is the case for `sample`, dogfooding
     * against the current sources within this monorepo. Any external
     * consumer's build has no such subproject, so it transparently falls
     * back to the published Maven coordinates instead.
     */
    private fun configureDependencies(project: Project) {
        project.dependencies {
            add("commonMainImplementation", localProjectOrCoordinates(project, "cstories-annotations"))
            add("commonMainImplementation", localProjectOrCoordinates(project, "cstories-runtime"))
            add("kspCommonMainMetadata", localProjectOrCoordinates(project, "cstories-processor"))
        }
    }

    private fun localProjectOrCoordinates(project: Project, moduleName: String): Any {
        return project.rootProject.findProject(":$moduleName")
            ?: "io.cstories:$moduleName:$CSTORIES_VERSION"
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

    /**
     * Both the production distribution and the development webpack bundle
     * get a manifest listing all files they produced, written next to
     * `index.html`. The catalog's Export button reads this manifest at
     * runtime to build a standalone zip client-side (see
     * `cstories-runtime`'s `triggerStandaloneExport`).
     *
     * For `wasmJsBrowserDistribution`, everything (resources + compiled
     * bundle) already lands in the same `dist` directory, so the manifest
     * task simply lists that one directory and finalizes the distribution
     * task since it's a single terminating task run.
     *
     * For the dev workflow (`runCStories` / `wasmJsBrowserDevelopmentRun`),
     * things are split: the dev server serves static files (`index.html`,
     * compose resources) from `build/processedResources/wasmJs/main`, but
     * the compiled JS/wasm bundle is only ever produced by the dev server's
     * own in-process webpack-dev-middleware — there's no prior finished
     * Gradle task whose output directory reflects what gets served. To work
     * around this, `wasmJsBrowserDevelopmentRun` is made to *depend on*
     * `wasmJsBrowserDevelopmentWebpack` (which normally isn't in its task
     * graph) so a first webpack compilation happens through Gradle, writing
     * the bundle to `build/kotlin-webpack/wasmJs/developmentExecutable`.
     * webpack's content hashes are deterministic for identical sources, so
     * when the dev server subsequently compiles the same sources itself, it
     * reproduces the exact same file names. The manifest task then lists
     * files from *both* directories, but writes the manifest itself into
     * `processedResources/wasmJs/main`, since that's the directory the dev
     * server actually serves static files from.
     */
    private fun registerStandaloneManifestTask(project: Project) {
        val productionDistDir = project.layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
        val productionManifestTask = project.tasks.register(
            "cstoriesGenerateStandaloneManifest",
            CStoriesStandaloneManifestTask::class.java,
        ) {
            group = "cstories"
            description = "Writes a manifest of the wasmJs production distribution files, used by the Export button"
            siteDirectories.from(productionDistDir)
            manifestOutputDirectory.set(productionDistDir)
        }

        project.tasks.matching { it.name == "wasmJsBrowserDistribution" }.configureEach {
            finalizedBy(productionManifestTask)
        }

        val developmentResourcesDir = project.layout.buildDirectory.dir("processedResources/wasmJs/main")
        val developmentBundleDir = project.layout.buildDirectory.dir("kotlin-webpack/wasmJs/developmentExecutable")
        val developmentManifestTask = project.tasks.register(
            "cstoriesGenerateStandaloneDevelopmentManifest",
            CStoriesStandaloneManifestTask::class.java,
        ) {
            group = "cstories"
            description = "Writes a manifest of the wasmJs development bundle files, used by the Export button"
            siteDirectories.from(developmentResourcesDir, developmentBundleDir)
            manifestOutputDirectory.set(developmentResourcesDir)
            dependsOn("wasmJsBrowserDevelopmentWebpack")
        }

        project.tasks.matching { it.name == "wasmJsBrowserDevelopmentRun" }.configureEach {
            dependsOn(developmentManifestTask)
        }
    }
}

private const val MODULE_NAME_OPTION = "cstories.moduleName"

/**
 * Version of the published `io.cstories:*` artifacts to depend on when no
 * local sibling subproject is found. Must be kept in sync with the root
 * build's `allprojects { version = ... }` (this is a separate, included
 * Gradle build, so it can't share that declaration directly).
 */
private const val CSTORIES_VERSION = "0.1.0-SNAPSHOT"
