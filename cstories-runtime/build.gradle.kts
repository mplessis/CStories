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
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
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
