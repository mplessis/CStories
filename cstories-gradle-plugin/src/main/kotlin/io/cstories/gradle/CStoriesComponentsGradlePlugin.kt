package io.cstories.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Lightweight counterpart to [CStoriesGradlePlugin], meant for plain
 * component/design-system library modules (as opposed to the `@CStory`
 * catalog module itself).
 *
 * `@CStoryComponent` is only ever visible to KSP within the module it
 * annotates functions in — KSP never scans symbols across a dependency
 * boundary, so a catalog module (applying [CStoriesGradlePlugin]) can't
 * generate refs for components declared in a *different* module it merely
 * depends on. Applying this plugin directly on that other module instead
 * generates `io.cstories.generated.CStoryComponentRefs` locally, which the
 * catalog module can then simply import like any other dependency symbol.
 *
 * Unlike [CStoriesGradlePlugin], this plugin does not apply Compose
 * Multiplatform, does not require specific `jvm()`/`wasmJs()` targets, and
 * does not wire any catalog/entry-point/aggregation task — it only adds the
 * `cstories-annotations` dependency and wires KSP to process
 * `@CStoryComponent`.
 */
class CStoriesComponentsGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = requireNotNull(project.extensions.findByType<KotlinMultiplatformExtension>()) {
            "CStories requires the Kotlin Multiplatform plugin"
        }

        project.dependencies.add(
            "commonMainImplementation",
            localProjectOrCoordinates(project, "cstories-annotations"),
        )

        project.extensions.findByType(KspExtension::class.java)?.apply {
            arg(MODULE_NAME_OPTION, project.name)
        }

        project.wireComponentRefsGeneration(kotlin)
    }
}
