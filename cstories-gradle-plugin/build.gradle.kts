plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.multiplatform.get()}")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}

gradlePlugin {
    plugins {
        create("cstories") {
            id = "io.cstories.gradle"
            implementationClass = "io.cstories.gradle.CStoriesGradlePlugin"
            displayName = "CStories Gradle Plugin"
            description = "Configures CStories for Compose Multiplatform projects"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
