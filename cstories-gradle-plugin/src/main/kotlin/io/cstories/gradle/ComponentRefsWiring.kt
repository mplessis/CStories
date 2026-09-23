package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import java.util.jar.JarFile
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Wires KSP so that `@CStoryComponent`-annotated functions declared in
 * `commonMain` get processed into a `CStoryComponentRefs` object, visible
 * from `commonMain` regardless of how many platform targets the module
 * declares.
 *
 * Shared between [CStoriesGradlePlugin] (catalog modules, which also process
 * `@CStory`) and [CStoriesComponentsGradlePlugin] (plain component library
 * modules, which only ever need `@CStoryComponent` processed). KSP only
 * scans annotated symbols within the module it runs against — never across
 * a dependency boundary — so both plugins need this same wiring, applied
 * directly to whichever module actually declares the annotated functions.
 */
internal fun Project.wireComponentRefsGeneration(
    kotlin: KotlinMultiplatformExtension,
    readDependencyMetadata: Boolean = false,
) {
    fun realTargets() = kotlin.targets.filter { it.platformType != KotlinPlatformType.common }

    // KSP snapshots the `kspCommonMainMetadata` configuration's dependencies
    // eagerly, as soon as more than one real target exists (see
    // `CStoriesGradlePlugin` for the full explanation) — added reactively,
    // the instant a second real target is declared, never from
    // `afterEvaluate` (too late for KSP to notice it).
    var addedCommonMetadataDependency = false
    kotlin.targets.configureEach {
        if (!addedCommonMetadataDependency && realTargets().size > 1) {
            addedCommonMetadataDependency = true
            dependencies.add(
                "kspCommonMainMetadata",
                localProjectOrCoordinates(this@wireComponentRefsGeneration, "cstories-processor"),
            )
        }
    }

    // KSP's `arg(...)` extension option is global to every ksp task, so the
    // only way to tell the processor "you're running against commonMain
    // metadata, not a concrete platform" is a task-scoped command line
    // argument set directly on that one task.
    tasks.matching { it.name == "kspCommonMainKotlinMetadata" }.configureEach {
        val provider = CommandLineArgumentProvider { listOf("$PROCESS_MODE_OPTION=common") }
        (this as? KspTask)?.commandLineArgumentProviders?.add(provider)
        (this as? KspAATask)?.commandLineArgumentProviders?.add(provider)
    }

    // The ksp Gradle plugin only wires `kspCommonMainKotlinMetadata`'s
    // output as a source of the metadata compile task itself (with an
    // automatic task dependency), never as a dependency of the platform
    // compile tasks — that has to be done explicitly, or Gradle would never
    // actually schedule `kspCommonMainKotlinMetadata` at all (nothing else
    // in the task graph would reference it).
    val commonMetadataKspTasks = tasks.matching { it.name == "kspCommonMainKotlinMetadata" }

    val componentMetadata = if (readDependencyMetadata) {
        tasks.register<ExtractComponentMetadataTask>("cstoriesExtractComponentMetadata") {
            configurations.matching { it.name.endsWith("CompileClasspath") }.forEach { configuration ->
                val artifactFiles = configuration.incoming.artifactView {
                    componentFilter { identifier ->
                        identifier !is ProjectComponentIdentifier || identifier.projectPath != project.path
                    }
                }.files
                // Detach the snapshot consumed by the extractor from KMP's
                // own-output task dependencies. Those dependencies include
                // this project's compilation and create a cycle with KSP.
                inputClasspath.from(files(artifactFiles.files))
                dependsOn(artifactFiles.buildDependencies.getDependencies(this).filter { it.project != project })
            }
            outputFile.set(layout.buildDirectory.file("generated/cstories/component-metadata/components.txt"))
        }
    } else {
        null
    }
    val metadataArgument = componentMetadata?.let { task ->
        CommandLineArgumentProvider { listOf("$COMPONENT_METADATA_OPTION=${task.get().outputFile.get().asFile.absolutePath}") }
    }
    tasks.withType<KspTask>().configureEach {
        metadataArgument?.let(commandLineArgumentProviders::add)
        componentMetadata?.let { dependsOn(it) }
        if (!readDependencyMetadata) {
            commandLineArgumentProviders.add(CommandLineArgumentProvider { listOf("$WRITE_COMPONENT_METADATA_OPTION=true") })
        }
    }
    tasks.withType<KspAATask>().configureEach {
        metadataArgument?.let(commandLineArgumentProviders::add)
        componentMetadata?.let { dependsOn(it) }
        if (!readDependencyMetadata) {
            commandLineArgumentProviders.add(CommandLineArgumentProvider { listOf("$WRITE_COMPONENT_METADATA_OPTION=true") })
        }
    }
    kotlin.targets.configureEach {
        if (platformType == KotlinPlatformType.common) return@configureEach
        compilations.configureEach {
            if (name == "main") {
                compileTaskProvider.configure { dependsOn(commonMetadataKspTasks) }
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        if (name.endsWith("KotlinAndroid")) {
            dependsOn(commonMetadataKspTasks)
        }
    }

    if (!readDependencyMetadata) {
        tasks.withType<ProcessResources>().configureEach {
            dependsOn(commonMetadataKspTasks)
        }
    }

    // A dedicated, stable lifecycle task so consumers can regenerate
    // `CStoryComponentRefs` after adding/removing a `@CStoryComponent`
    // without needing to run a full build. Depends only on whichever KSP
    // task actually performs the processing (`kspCommonMainKotlinMetadata`
    // for multi-target modules, wired here; the single-target "standalone"
    // task is added below, once `afterEvaluate` knows which case applies).
    val generateComponentRefs = tasks.register("cstoriesGenerateComponentRefs") {
        group = "cstories"
        description = "Generates CStoryComponentRefs for @CStoryComponent-annotated functions"
        dependsOn(commonMetadataKspTasks)
    }

    // Once the generated metadata directory is added onto `commonMain`
    // below, every platform sources jar reads that same directory too.
    // Gradle 8.13 validates that such cross-task file usage is backed by an
    // explicit dependency, which KSP/Kotlin do not infer automatically here.
    val sourcesJarTasks = tasks.matching {
        it.name == "sourcesJar" || it.name.endsWith("SourcesJar")
    }
    sourcesJarTasks.configureEach {
        dependsOn(commonMetadataKspTasks)
    }

    // The ksp Gradle plugin only wires `kspCommonMainKotlinMetadata`'s
    // output onto the metadata compile task itself, never onto the
    // `commonMain` source set — needed here so every platform target
    // (which each pull in `commonMain` as a dependency source set) can
    // actually see the generated `CStoryComponentRefs`.
    if (readDependencyMetadata) {
        kotlin.sourceSets.getByName("commonMain").kotlin
            .srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
    } else {
        kotlin.sourceSets.getByName("commonMain").resources
            .srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/resources"))
    }

    // The consumer declares its targets in its own `kotlin { }` block, which
    // runs after this plugin is applied — detecting how many real targets
    // actually got declared therefore has to be deferred until the project
    // is fully configured.
    afterEvaluate {
        val targets = realTargets()
        if (targets.size != 1) {
            targets.forEach { target ->
                val targetTaskName = "kspKotlin" + target.name.replaceFirstChar(Char::uppercaseChar)
                tasks.matching { it.name == targetTaskName }.configureEach {
                    val provider = CommandLineArgumentProvider { listOf("$PROCESS_MODE_OPTION=stories") }
                    (this as? KspTask)?.commandLineArgumentProviders?.add(provider)
                    (this as? KspAATask)?.commandLineArgumentProviders?.add(provider)
                }
            }
            return@afterEvaluate
        }

        // With a single real target declared, Kotlin never creates a
        // `kspCommonMainKotlinMetadata` task at all (no separate metadata
        // compilation is needed when only one target consumes `commonMain`)
        // — so `@CStoryComponent` refs would otherwise never get generated.
        // In that case, let the one and only per-target ksp run handle it
        // instead ("standalone" mode).
        val target = targets.single()
        val standaloneTaskName = "ksp" + "Kotlin" + target.name.replaceFirstChar(Char::uppercaseChar)
        val standaloneKspTasks = tasks.matching { it.name == standaloneTaskName }
        standaloneKspTasks.configureEach {
            val provider = CommandLineArgumentProvider { listOf("$PROCESS_MODE_OPTION=standalone") }
            (this as? KspTask)?.commandLineArgumentProviders?.add(provider)
            (this as? KspAATask)?.commandLineArgumentProviders?.add(provider)
        }
        generateComponentRefs.configure { dependsOn(standaloneKspTasks) }
        sourcesJarTasks.configureEach { dependsOn(standaloneKspTasks) }

        if (!readDependencyMetadata) {
            val standaloneMain = kotlin.sourceSets.getByName("${target.name}Main")
            standaloneMain.resources.srcDir(
                layout.buildDirectory.dir("generated/ksp/${target.name}/${target.name}Main/resources"),
            )
            tasks.matching { it.name == "${target.name}ProcessResources" }.configureEach {
                dependsOn(standaloneKspTasks)
            }
            return@afterEvaluate
        }

        // The generated refs directory is moved from the target source set to
        // commonMain below. Preserve the task ordering that KSP normally adds
        // for the original target source set explicitly, otherwise the
        // commonMain compilation can start before CStoryComponentRefs exists.
        kotlin.targets.getByName(target.name).compilations.getByName("main")
            .compileTaskProvider
            .configure { dependsOn(standaloneKspTasks) }

        // The ksp Gradle plugin always wires that per-target run's output
        // onto the target's own platform source set — never onto
        // `commonMain`, even for a single-target consumer. Kotlin still
        // enforces the usual source-set/fragment boundary in that case (a
        // file can only ever belong to one fragment), so code written in
        // `commonMain` (the normal, recommended place for it) would
        // otherwise fail to resolve `CStoryComponentRefs`, generated into
        // the platform-only source set instead. Moving that same physical
        // directory onto `commonMain` instead (rather than merely also
        // adding it there, which the Kotlin compiler rejects as "can be a
        // part of only one module") makes it visible from `commonMain`.
        // Since a single-target consumer folds `commonMain` directly into
        // that one target's own compilation anyway, generated declarations
        // remain just as reachable from platform-specific code too.
        val generatedDir = layout.buildDirectory
            .dir("generated/ksp/${target.name}/${target.name}Main/kotlin")
            .get()
            .asFile
        val standaloneMain = kotlin.sourceSets.getByName("${target.name}Main")
        standaloneMain.kotlin.setSrcDirs(standaloneMain.kotlin.srcDirs.filterNot { it == generatedDir })
        kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generatedDir)
    }
}

internal fun localProjectOrCoordinates(project: Project, moduleName: String): Any {
    return project.rootProject.findProject(":$moduleName")
        ?: "dev.cstories:$moduleName:$CSTORIES_VERSION"
}

internal const val PROCESS_MODE_OPTION = "cstories.processMode"
internal const val MODULE_NAME_OPTION = "cstories.moduleName"
internal const val COMPONENT_METADATA_OPTION = "cstories.componentMetadata"
internal const val WRITE_COMPONENT_METADATA_OPTION = "cstories.writeComponentMetadata"

abstract class ExtractComponentMetadataTask : DefaultTask() {
    @get:Internal
    abstract val inputClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun extract() {
        val lines = inputClasspath.files.flatMap { file ->
            when {
                file.isDirectory -> File(file, ComponentMetadataPath).takeIf(File::exists)?.readLines().orEmpty()
                file.extension == "jar" -> JarFile(file).use { jar ->
                    jar.getJarEntry(ComponentMetadataPath)?.let { jar.getInputStream(it).bufferedReader().readLines() }.orEmpty()
                }
                else -> emptyList()
            }
        }
        outputFile.get().asFile.apply { parentFile.mkdirs() }.writeText(lines.joinToString("\n"))
    }

    private companion object {
        const val ComponentMetadataPath = "META-INF/cstories/components.txt"
    }
}
