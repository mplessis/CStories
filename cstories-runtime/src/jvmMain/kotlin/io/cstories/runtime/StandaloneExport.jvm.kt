package io.cstories.runtime

/**
 * The standalone export concept only makes sense for the wasmJs catalog
 * served as a static site — there is nothing to export when previewing on
 * the JVM target during development.
 */
actual suspend fun triggerStandaloneExport(): StandaloneExportResult = StandaloneExportResult.NotAvailable
