import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm()
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":cstories-annotations"))
            implementation(project(":cstories-runtime"))
            implementation(compose.material3)
        }
    }
}

dependencies {
    // Keep the sample wired manually inside this monorepo. The Gradle plugin
    // is validated independently and will be exercised from a consumer-style
    // build once repository-local plugin resolution is set up.
    add("kspCommonMainMetadata", project(":cstories-processor"))
}
