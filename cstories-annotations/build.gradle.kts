plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    wasmJs {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
        }
    }
}
