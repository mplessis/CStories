plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("io.cstories.gradle")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
        }
    }
}
