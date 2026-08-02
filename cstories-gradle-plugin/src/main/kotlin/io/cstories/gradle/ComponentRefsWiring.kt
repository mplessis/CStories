package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspTask
import org.gradle.api.Project
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

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
internal fun Project.wireComponentRefsGeneration(kotlin: KotlinMultiplatformExtension) {
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
    kotlin.targets.configureEach {
        if (platformType == KotlinPlatformType.common) return@configureEach
        compilations.configureEach {
            if (name == "main") {
                compileTaskProvider.configure { dependsOn(commonMetadataKspTasks) }
            }
        }
    }

    // The ksp Gradle plugin only wires `kspCommonMainKotlinMetadata`'s
    // output onto the metadata compile task itself, never onto the
    // `commonMain` source set — needed here so every platform target
    // (which each pull in `commonMain` as a dependency source set) can
    // actually see the generated `CStoryComponentRefs`.
    kotlin.sourceSets.getByName("commonMain").kotlin
        .srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))

    // The consumer declares its targets in its own `kotlin { }` block, which
    // runs after this plugin is applied — detecting how many real targets
    // actually got declared therefore has to be deferred until the project
    // is fully configured.
    afterEvaluate {
        val targets = realTargets()
        if (targets.size != 1) return@afterEvaluate

        // With a single real target declared, Kotlin never creates a
        // `kspCommonMainKotlinMetadata` task at all (no separate metadata
        // compilation is needed when only one target consumes `commonMain`)
        // — so `@CStoryComponent` refs would otherwise never get generated.
        // In that case, let the one and only per-target ksp run handle it
        // instead ("standalone" mode).
        val target = targets.single()
        val standaloneTaskName = "ksp" + "Kotlin" + target.name.replaceFirstChar(Char::uppercaseChar)
        tasks.matching { it.name == standaloneTaskName }.configureEach {
            val provider = CommandLineArgumentProvider { listOf("$PROCESS_MODE_OPTION=standalone") }
            (this as? KspTask)?.commandLineArgumentProviders?.add(provider)
            (this as? KspAATask)?.commandLineArgumentProviders?.add(provider)
        }

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
        ?: "io.cstories:$moduleName:$CSTORIES_VERSION"
}

internal const val PROCESS_MODE_OPTION = "cstories.processMode"
internal const val MODULE_NAME_OPTION = "cstories.moduleName"
