package io.cstories.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.file.RegularFileProperty
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComponentRefsWiringTest {
    @Test
    fun `metadata extraction preserves repeated encoded fields`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("extract", ExtractComponentMetadataTask::class.java)
        val input = project.layout.buildDirectory.file("input/META-INF/cstories/components.txt").get().asFile
        input.parentFile.mkdirs()
        input.writeText("same\nsame\nname\nfqn\ndoc\nsame\nsame\nother\nfqn2\ndoc2\n")
        task.configure {
            inputClasspath.from(input.parentFile.parentFile.parentFile)
            outputFile.set(project.layout.buildDirectory.file("output/components.txt"))
        }

        task.get().actions.forEach { it.execute(task.get()) }

        assertEquals(
            input.readText().trimEnd(),
            task.get().outputFile.get().asFile.readText().trimEnd(),
        )
    }

    @Test
    fun `components plugin keeps annotations compile only`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("dev.cstories.gradle.components")

        val compileOnlyDependencies = project.configurations
            .getByName("commonMainCompileOnly")
            .dependencies
            .map { it.group to it.name }
        val implementationDependencies = project.configurations
            .getByName("commonMainImplementation")
            .dependencies
            .map { it.group to it.name }

        assertTrue("dev.cstories" to "cstories-annotations" in compileOnlyDependencies)
        assertFalse("dev.cstories" to "cstories-annotations" in implementationDependencies)
        assertEquals(1, compileOnlyDependencies.count { it == "dev.cstories" to "cstories-annotations" })
    }

    @Test
    fun `components plugin does not add generated refs to commonMain`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.cstories.gradle.components")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()

        val commonMain = kotlin.sourceSets.getByName("commonMain")
        assertFalse(commonMain.kotlin.srcDirs.any { it.path.contains("generated/ksp") })
    }

    @Test
    fun `android release sources jar depends on metadata ksp task when both exist`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()
        kotlin.iosX64()

        val metadataTask = project.tasks.register("kspCommonMainKotlinMetadata")
        val sourcesJarTask = project.tasks.register("androidReleaseSourcesJar", Jar::class.java)

        project.wireComponentRefsGeneration(kotlin)

        val dependencies = taskDependenciesOf(sourcesJarTask.get())
        assertTrue(metadataTask.get() in dependencies)
    }

    @Test
    fun `desktop sources jar depends on metadata ksp task when both exist`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()
        kotlin.iosX64()

        val metadataTask = project.tasks.register("kspCommonMainKotlinMetadata")
        val sourcesJarTask = project.tasks.register("desktopSourcesJar", Jar::class.java)

        project.wireComponentRefsGeneration(kotlin)

        val dependencies = taskDependenciesOf(sourcesJarTask.get())
        assertTrue(metadataTask.get() in dependencies)
    }

    @Test
    fun `common sources jar depends on metadata ksp task when both exist`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()
        kotlin.iosX64()

        val metadataTask = project.tasks.register("kspCommonMainKotlinMetadata")
        val sourcesJarTask = project.tasks.register("sourcesJar", Jar::class.java)

        project.wireComponentRefsGeneration(kotlin)

        val dependencies = taskDependenciesOf(sourcesJarTask.get())
        assertTrue(metadataTask.get() in dependencies)
    }

    @Test
    fun `missing android sources jar task does not fail configuration`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()
        kotlin.iosX64()

        project.tasks.register("kspCommonMainKotlinMetadata")

        project.wireComponentRefsGeneration(kotlin)

        assertTrue(project.tasks.findByName("androidReleaseSourcesJar") == null)
    }

    @Test
    fun `missing metadata ksp task does not add bogus android dependency`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()

        val sourcesJarTask = project.tasks.register("androidReleaseSourcesJar", Jar::class.java)

        project.wireComponentRefsGeneration(kotlin)

        val dependencyNames = taskDependenciesOf(sourcesJarTask.get()).map(Task::getName)
        assertFalse("kspCommonMainKotlinMetadata" in dependencyNames)
    }

    @Test
    fun `components plugin does not expose generated refs through commonMain`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.google.devtools.ksp")

        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm()
        kotlin.iosX64()

        project.wireComponentRefsGeneration(kotlin)

        val commonMain = kotlin.sourceSets.getByName("commonMain")
        val generatedDirSuffix = "build/generated/ksp/metadata/commonMain/kotlin"
        assertFalse(commonMain.kotlin.srcDirs.any { it.path.endsWith(generatedDirSuffix) })
    }

    private fun taskDependenciesOf(task: Task): Set<Task> {
        return task.taskDependencies.getDependencies(task)
    }
}
