package io.cstories.runtime

// The catalog app never actually runs on iOS (see cstories-runtime's
// build.gradle.kts); these actuals only exist so commonMain compiles for
// every published target.
internal actual val hotReloadCommand: String = ""

internal actual fun isHotReloadActive(): Boolean = false
