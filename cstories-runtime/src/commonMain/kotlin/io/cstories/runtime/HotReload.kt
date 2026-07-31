package io.cstories.runtime

/**
 * The command to advertise in [PromoCard][io.cstories.runtime] so consumers
 * can start a fast development loop, tailored to the current target:
 * Compose Hot Reload on JVM, `--continuous` on wasmJs.
 */
internal expect val hotReloadCommand: String

/**
 * Whether the app is currently running under Compose Hot Reload. Only
 * detectable on JVM — used to hide the promo card once the developer is
 * already using the workflow it recommends.
 */
internal expect fun isHotReloadActive(): Boolean
