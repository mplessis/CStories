import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm()
    wasmJs {
        browser()
    }
    // Story functions live in a consumer's commonMain and directly call
    // this module's `knobs` composables (KnobPanel, TextKnob, ...), so
    // cstories-runtime must publish every target the consumer's shared
    // module might target — even though the catalog app itself only ever
    // actually runs on wasmJs — otherwise resolving commonMain metadata
    // for those other targets fails. Kept in sync with the target list of
    // cstories-annotations.
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Exposed as `api`: the public API (StoryEntry, CStoriesThemeWrapper,
            // knobs, previews) is made of @Composable functions and Modifier
            // parameters that story modules compile against.
            api(libs.compose.runtime)
            api(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }

        jvmMain.dependencies {
            implementation(libs.compose.hot.reload.runtime.api)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources {
    packageOfResClass = "io.cstories.runtime.resources"
}
