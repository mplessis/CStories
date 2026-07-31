package io.cstories.runtime

internal actual val hotReloadCommand: String = "./gradlew runCStoriesWasm --continuous"

// The `--continuous` flag only drives Gradle's build watcher; it is never
// forwarded to the running browser process, so there is no way to detect it
// at runtime here.
internal actual fun isHotReloadActive(): Boolean = false
