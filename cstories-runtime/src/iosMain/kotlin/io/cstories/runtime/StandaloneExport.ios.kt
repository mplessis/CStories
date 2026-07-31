package io.cstories.runtime

/**
 * The standalone export concept only makes sense for the wasmJs catalog
 * served as a static site — there is nothing to export when a consumer's
 * shared module also happens to compile its stories for iOS (this target
 * only needs to exist so that referencing `cstories-runtime`'s `knobs`
 * composables from commonMain story code resolves).
 */
actual suspend fun triggerStandaloneExport(): StandaloneExportResult = StandaloneExportResult.NotAvailable
