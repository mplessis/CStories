package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspAATask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.Task
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.named
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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
        val metadataConfiguration = configurations.create(COMPONENT_METADATA_CONFIGURATION) {
            isCanBeConsumed = false
            isCanBeResolved = true
            isVisible = false
            attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named(COMPONENT_METADATA_USAGE)) }
        }
        configurations
            .matching { it.name.endsWith("MainImplementation") || it.name.endsWith("MainApi") }
            .configureEach { metadataConfiguration.extendsFrom(this) }
        // Lenient: dependencies that don't apply the components plugin (and
        // every external module) simply have no such variant.
        val metadataArtifacts = metadataConfiguration.incoming.artifactView { lenient(true) }.artifacts
        tasks.register<ExtractComponentMetadataTask>("cstoriesExtractComponentMetadata") {
            metadataFiles.from(metadataArtifacts.artifactFiles)
            metadataSources.set(
                metadataArtifacts.resolvedArtifacts.map { artifacts ->
                    artifacts.associate { it.file.absolutePath to it.id.componentIdentifier.displayName }
                },
            )
            configurations.matching { it.name.endsWith("CompileClasspath") }.forEach { configuration ->
                val artifactFiles = configuration.incoming.artifactView {
                    componentFilter { identifier ->
                        identifier !is ProjectComponentIdentifier || identifier.projectPath != project.path
                    }
                }.files
                // Detach the snapshot consumed by the extractor from KMP's
                // own-output task dependencies. Those dependencies include
                // this project's compilation and create a cycle with KSP.
                // Own outputs (e.g. main classes on test classpaths) are also
                // dropped: they change after KSP runs and would otherwise
                // keep this task permanently out of date.
                val ownBuildDir = layout.buildDirectory.get().asFile
                inputClasspath.from(files(artifactFiles.files.filterNot { it.startsWith(ownBuildDir) }))
                dependsOn(artifactFiles.buildDependencies.getDependencies(this).filter { it.project != project })
            }
            outputFile.set(layout.buildDirectory.file("generated/cstories/component-metadata/components.txt"))
        }
    } else {
        null
    }
    val metadataArgument = componentMetadata?.let { task ->
        objects.newInstance(ComponentMetadataArgumentProvider::class.java).apply {
            metadataFile.set(task.flatMap { it.outputFile })
        }
    }
    tasks.withType<KspAATask>().configureEach {
        metadataArgument?.let { provider ->
            commandLineArgumentProviders.add(provider)
            // The task reruns whenever components.txt changes (it is an
            // input of the argument provider), but KSP's incremental pass
            // only tracks source files, so a KDoc-only edit in a dependency
            // would otherwise never be reflected in the generated refs.
            kspConfig.incremental.set(false)
        }
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
        composeResourceCopyTasks().configureEach {
            dependsOn(commonMetadataKspTasks)
        }
        configurations.create(COMPONENT_METADATA_ELEMENTS_CONFIGURATION) {
            isCanBeConsumed = true
            isCanBeResolved = false
            isVisible = false
            attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named(COMPONENT_METADATA_USAGE)) }
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
            if (!readDependencyMetadata) {
                exposeComponentMetadata("generated/ksp/metadata/commonMain/resources", commonMetadataKspTasks)
            }
            targets.forEach { target ->
                val targetTaskName = "kspKotlin" + target.name.replaceFirstChar(Char::uppercaseChar)
                tasks.matching { it.name == targetTaskName }.configureEach {
                    val provider = CommandLineArgumentProvider { listOf("$PROCESS_MODE_OPTION=stories") }
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
                (this as? KspAATask)?.commandLineArgumentProviders?.add(provider)
        }
        generateComponentRefs.configure { dependsOn(standaloneKspTasks) }
        sourcesJarTasks.configureEach { dependsOn(standaloneKspTasks) }

        if (!readDependencyMetadata) {
            exposeComponentMetadata("generated/ksp/${target.name}/${target.name}Main/resources", standaloneKspTasks)
            val standaloneMain = kotlin.sourceSets.getByName("${target.name}Main")
            standaloneMain.resources.srcDir(
                layout.buildDirectory.dir("generated/ksp/${target.name}/${target.name}Main/resources"),
            )
            tasks.matching { it.name == "${target.name}ProcessResources" }.configureEach {
                dependsOn(standaloneKspTasks)
            }
            composeResourceCopyTasks().configureEach {
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
        // KSP may register that directory on the target source set lazily,
        // after this callback: exclude it there too, whenever it gets added.
        standaloneMain.kotlin.exclude { it.file.startsWith(generatedDir) }
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
internal const val COMPONENT_METADATA_ELEMENTS_CONFIGURATION = "cstoriesComponentMetadataElements"
internal const val COMPONENT_METADATA_CONFIGURATION = "cstoriesComponentMetadata"
internal const val COMPONENT_METADATA_USAGE = "cstories-component-metadata"
private const val COMPONENT_METADATA_PATH = "META-INF/cstories/components.txt"

/**
 * Exposes the KSP-written `components.txt` as a dedicated variant, so that a
 * stories module depending on this project receives it directly, whatever
 * KMP packages (or not) into its jars and klibs.
 */
private fun Project.exposeComponentMetadata(resourcesDir: String, producers: TaskCollection<Task>) {
    configurations.getByName(COMPONENT_METADATA_ELEMENTS_CONFIGURATION).outgoing.artifact(
        layout.buildDirectory.file("$resourcesDir/$COMPONENT_METADATA_PATH"),
    ) { builtBy(producers) }
}

// Compose Multiplatform copies every resources dir (including the KSP one
// added above) into its own tasks, which don't infer the KSP dependency.
private fun Project.composeResourceCopyTasks() =
    tasks.matching { it.name.startsWith("copy") && it.name.contains("ComposeResources") }

/**
 * Passes the extracted component metadata file to KSP. Only the path is
 * passed (known at configuration time), and the file itself is declared as
 * an input, which also wires the dependency on the extraction task in a
 * configuration-cache compatible way.
 */
abstract class ComponentMetadataArgumentProvider : CommandLineArgumentProvider {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val metadataFile: RegularFileProperty

    override fun asArguments(): Iterable<String> =
        listOf("$COMPONENT_METADATA_OPTION=${metadataFile.get().asFile.absolutePath}")
}

abstract class ExtractComponentMetadataTask : DefaultTask() {
    @get:Classpath
    abstract val inputClasspath: ConfigurableFileCollection

    /** `components.txt` files received through the dedicated metadata variant. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val metadataFiles: ConfigurableFileCollection

    /** Absolute path of each [metadataFiles] entry to the dependency providing it. */
    @get:Internal
    abstract val metadataSources: MapProperty<String, String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun extract() {
        val sources = metadataSources.getOrElse(emptyMap())
        val variantRecords = metadataFiles.files.flatMap { file ->
            if (file.isFile) {
                file.readLines().toRecords()
            } else {
                logger.warn(
                    "CStories: '${sources[file.absolutePath] ?: file}' applies dev.cstories.gradle.components " +
                        "but no CStories component metadata was found ($file): either it declares no " +
                        "@CStoryComponent, or its metadata was not generated and its components will be missing " +
                        "from CStoryComponentRefs.",
                )
                emptyList()
            }
        }
        val classpathRecords = inputClasspath.files.flatMap { file ->
            when {
                file.isDirectory -> File(file, COMPONENT_METADATA_PATH).takeIf(File::exists)?.readLines().orEmpty().toRecords()
                file.extension == "jar" -> JarFile(file).use { jar ->
                    jar.getJarEntry(COMPONENT_METADATA_PATH)?.let { jar.getInputStream(it).bufferedReader().readLines() }.orEmpty().toRecords()
                }
                else -> emptyList()
            }
        }
        // A project dependency can be seen both through its variant and its
        // jars: keep each 5-line component record once.
        val lines = (variantRecords + classpathRecords).distinct().flatten()
        outputFile.get().asFile.apply { parentFile.mkdirs() }.writeText(lines.joinToString("") { "$it\n" })
    }

    // Fields may be empty (blank lines), so records are split per file.
    private fun List<String>.toRecords() = chunked(5).filter { it.size == 5 }
}
