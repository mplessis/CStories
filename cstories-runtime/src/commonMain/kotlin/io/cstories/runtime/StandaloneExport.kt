package io.cstories.runtime

/**
 * Result of attempting to export a standalone, self-contained copy of the
 * currently running catalog as a downloadable zip.
 */
sealed class StandaloneExportResult {
    /** The zip was successfully built and a browser download was triggered. */
    data object Success : StandaloneExportResult()

    /**
     * No `cstories-manifest.json` was found next to the app — this happens
     * when running a development server instead of a production
     * `wasmJsBrowserDistribution` build, since the manifest is only
     * generated for that distribution.
     */
    data object NotAvailable : StandaloneExportResult()

    /** Something else went wrong while fetching files or building the zip. */
    data class Failure(val message: String) : StandaloneExportResult()
}

/**
 * Builds a standalone, self-contained copy of the currently running
 * catalog site as a zip, entirely client-side, and triggers a browser
 * download of it.
 *
 * Implementations read a `cstories-manifest.json` file (generated at build
 * time by `cstories-gradle-plugin` next to `index.html`) listing every file
 * of the distribution, fetch each one, and zip them together in-browser —
 * no server-side build step is involved at export time.
 */
expect suspend fun triggerStandaloneExport(): StandaloneExportResult
