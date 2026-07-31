package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
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

        configureCommonDependencies(project)
        configureKsp(project)

        // KSP snapshots each target's own `ksp<Target>ProcessorClasspath`
        // configuration *eagerly*, as soon as that target's compile task is
        // registered (which happens synchronously when the consumer calls
        // e.g. `jvm()`/`wasmJs()` in their own `kotlin { }` block — itself
        // evaluated right after this plugin is applied). Adding our
        // processor dependency inside a `project.afterEvaluate` would run
        // too late for KSP to notice it, silently turning every `ksp<Target>`
        // task into a no-op. `kotlin.targets.configureEach` instead fires
        // immediately for every target as soon as it's declared (whether
        // that happens before or after this line), which is early enough.
        kotlin.targets.configureEach {
            if (name == "jvm" || name == "wasmJs") {
                val kspConfigurationName = "ksp" + name.replaceFirstChar(Char::uppercaseChar)
                project.dependencies.add(
                    kspConfigurationName,
                    localProjectOrCoordinates(project, "cstories-processor"),
                )
            }
        }

        // The consumer declares its jvm()/wasmJs() targets in the `kotlin { }`
        // block of its own build script, which runs *after* this plugin is
        // applied (plugins {} blocks are evaluated first). Detecting which
        // targets actually got declared therefore has to be deferred until
        // the project is fully configured.
        project.afterEvaluate {
            val hasWasmJs = kotlin.targets.findByName("wasmJs") != null
            val hasJvm = kotlin.targets.findByName("jvm") != null
            if (!hasWasmJs && !hasJvm) {
                throw GradleException(
                    "CStories requires the consumer project to declare at least a jvm() or " +
                        "wasmJs { browser(); binaries.executable() } Kotlin Multiplatform target " +
                        "before applying the io.cstories.gradle plugin",
                )
            }

            val aggregateTask = registerAggregateTask(project, kotlin, hasWasmJs, hasJvm)
            if (hasWasmJs) {
                configureWasm(project, kotlin, aggregateTask)
            }
            if (hasJvm) {
                configureDesktop(project, kotlin, aggregateTask)
            }
        }
    }

    /**
     * Prefers a sibling subproject (`:cstories-annotations`, ...) when this
     * build actually has one — which is the case for `sample`, dogfooding
     * against the current sources within this monorepo. Any external
     * consumer's build has no such subproject, so it transparently falls
     * back to the published Maven coordinates instead.
     */
    private fun configureCommonDependencies(project: Project) {
        project.dependencies {
            add("commonMainImplementation", localProjectOrCoordinates(project, "cstories-annotations"))
            add("commonMainImplementation", localProjectOrCoordinates(project, "cstories-runtime"))
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

    private fun registerAggregateTask(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        hasWasmJs: Boolean,
        hasJvm: Boolean,
    ): TaskProvider<CStoriesAggregateTask> {
        return project.tasks.register<CStoriesAggregateTask>("cstoriesAggregateRegistries") {
            packageName.set("io.cstories.generated")
            jsBundleBaseName.set(project.name)
            generateWasmJsEntryPoint.set(hasWasmJs)
            generateDesktopEntryPoint.set(hasJvm)
            wasmJsOutputDirectory.set(project.layout.buildDirectory.dir(WASM_JS_OUTPUT_PATH))
            desktopOutputDirectory.set(project.layout.buildDirectory.dir(DESKTOP_OUTPUT_PATH))
            webResourcesDirectory.set(project.layout.buildDirectory.dir(WEB_RESOURCES_PATH))
            // KSP writes the manifest for this module's own stories into its
            // own target-specific generated resources output, which is not
            // part of any platform runtime classpath. Scan it explicitly so
            // local stories are always picked up, in addition to any
            // transitive dependency jars already published with the
            // manifest bundled in.
            if (hasJvm) {
                runtimeClasspath.from(project.layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/resources"))
                runtimeClasspath.from(project.configurations.getByName("jvmRuntimeClasspath"))
                dependsOn("kspKotlinJvm")
            }
            if (hasWasmJs) {
                runtimeClasspath.from(project.layout.buildDirectory.dir("generated/ksp/wasmJs/wasmJsMain/resources"))
                runtimeClasspath.from(project.configurations.getByName("wasmJsRuntimeClasspath"))
                dependsOn("kspKotlinWasmJs")
            }
        }
    }

    /**
     * Wires the generated wasmJs entry point (`CStoriesWasmJsEntryPoint.kt`),
     * the `runCStoriesWasm` alias task, and the standalone export manifest
     * tasks — the wasmJs/browser counterpart of [configureDesktop].
     *
     * Generated sources are wired using plain (non task-derived) directory
     * paths rather than `aggregateTask.map { ... }`. Gradle would otherwise
     * infer an automatic task dependency from *every* consumer of the
     * `wasmJsMain` source set — including that target's own KSP task — onto
     * [aggregateTask]. Since [aggregateTask] itself explicitly depends on
     * that same KSP task (to read its generated manifest), that automatic
     * inference would create a circular dependency. Ordering is instead
     * guaranteed explicitly, only on the actual Kotlin compile task.
     */
    private fun configureWasm(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        aggregateTask: TaskProvider<CStoriesAggregateTask>,
    ) {
        val wasmJsMain = kotlin.sourceSets.getByName("wasmJsMain")
        wasmJsMain.kotlin.srcDir(project.layout.buildDirectory.dir(WASM_JS_OUTPUT_PATH))
        wasmJsMain.resources.srcDir(project.layout.buildDirectory.dir(WEB_RESOURCES_PATH))
        project.tasks.matching { it.name == "compileKotlinWasmJs" }.configureEach {
            dependsOn(aggregateTask)
        }
        project.tasks.matching { it.name == "wasmJsProcessResources" }.configureEach {
            dependsOn(aggregateTask)
        }

        project.tasks.register("runCStoriesWasm") {
            group = "cstories"
            description = "Runs the generated CStories catalog in a browser"
            dependsOn("wasmJsBrowserDevelopmentRun")
        }

        registerStandaloneManifestTask(project)
    }

    /**
     * Wires the generated JVM entry point (`CStoriesDesktopEntryPoint.kt`)
     * and points the standard `jvmRun` task (and a `runCStoriesDesktop`
     * alias) at it, so the catalog can be run as a plain desktop window
     * without requiring the consumer to target `wasmJs`/a browser at all.
     */
    private fun configureDesktop(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        aggregateTask: TaskProvider<CStoriesAggregateTask>,
    ) {
        val jvmMain = kotlin.sourceSets.getByName("jvmMain")
        jvmMain.kotlin.srcDir(project.layout.buildDirectory.dir(DESKTOP_OUTPUT_PATH))
        project.tasks.matching { it.name == "compileKotlinJvm" }.configureEach {
            dependsOn(aggregateTask)
        }

        project.dependencies {
            add("jvmMainImplementation", desktopCurrentOs(project))
        }

        val desktopExtension = project.extensions.findByType(DesktopExtension::class.java)
        desktopExtension?.application?.mainClass = "io.cstories.generated.CStoriesDesktopEntryPointKt"

        // Compose Desktop's own packaging tasks are geared towards producing
        // an installable/distributable app. For a quick "preview the catalog"
        // loop, the plain `jvmRun` task Kotlin already registers for the jvm
        // target is simpler to run — just point its main class at the
        // generated entry point.
        project.tasks.matching { it.name == "jvmRun" }.configureEach {
            doFirst {
                (this as org.gradle.api.tasks.JavaExec).mainClass.set("io.cstories.generated.CStoriesDesktopEntryPointKt")
            }
        }

        project.tasks.register("runCStoriesDesktop") {
            group = "cstories"
            description = "Runs the generated CStories catalog as a desktop application"
            dependsOn("jvmRun")
        }
    }

    private fun desktopCurrentOs(project: Project): Any {
        val composeExtension = project.extensions.findByType(ComposeExtension::class.java)
        return composeExtension?.dependencies?.desktop?.currentOs
            ?: "org.jetbrains.compose.desktop:desktop-jvm:1.8.2"
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
     * For the dev workflow (`runCStoriesWasm` / `wasmJsBrowserDevelopmentRun`),
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
private const val WASM_JS_OUTPUT_PATH = "generated/cstories/wasmJsMain/kotlin"
private const val DESKTOP_OUTPUT_PATH = "generated/cstories/jvmMain/kotlin"
private const val WEB_RESOURCES_PATH = "generated/cstories/wasmJsMain/resources"
