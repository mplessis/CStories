package io.cstories.runtime

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.Promise

/**
 * `jszip`'s default export, declared as a real npm module import so that
 * webpack bundles it — this must not be replaced by ad-hoc `js(...)`
 * snippets referencing a global, since `jszip` is an ES module, not a
 * browser global.
 */
@JsModule("jszip")
private external class JsZip() : JsAny {
    fun file(path: String, data: JsAny)
    fun generateAsync(options: JsAny): Promise<JsAny?>
}

private fun blobGenerateOptions(): JsAny = js("({ type: 'blob' })")

private fun jsFetch(url: String): Promise<JsAny?> = js("fetch(url)")
private fun responseOk(response: JsAny): Boolean = js("response.ok")
private fun responseArrayBuffer(response: JsAny): Promise<JsAny?> = js("response.arrayBuffer()")
private fun responseJson(response: JsAny): Promise<JsAny?> = js("response.json()")

private fun manifestFilesArray(manifest: JsAny): JsAny = js("manifest.files")
private fun jsArrayLength(array: JsAny): Int = js("array.length")
private fun jsArrayGetString(array: JsAny, index: Int): JsString = js("array[index]")

private fun triggerBrowserDownload(blob: JsAny, fileName: String) {
    js(
        """
        (function() {
            var url = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        })()
        """,
    )
}

private const val MANIFEST_PATH = "cstories-manifest.json"
private const val STANDALONE_ZIP_FILE_NAME = "cstories-standalone.zip"

actual suspend fun triggerStandaloneExport(): StandaloneExportResult {
    return try {
        val manifestResponse: JsAny = jsFetch(MANIFEST_PATH).await()
        if (!responseOk(manifestResponse)) {
            return StandaloneExportResult.NotAvailable
        }

        val manifest: JsAny = responseJson(manifestResponse).await()
        val files = manifestFilesArray(manifest)
        val fileCount = jsArrayLength(files)

        val zip = JsZip()
        for (index in 0 until fileCount) {
            val path = jsArrayGetString(files, index).toString()
            val fileResponse: JsAny = jsFetch(path).await()
            if (!responseOk(fileResponse)) {
                return StandaloneExportResult.Failure("Failed to fetch $path")
            }
            val data: JsAny = responseArrayBuffer(fileResponse).await()
            zip.file(path, data)
        }

        val blob: JsAny = zip.generateAsync(blobGenerateOptions()).await()
        triggerBrowserDownload(blob, STANDALONE_ZIP_FILE_NAME)
        StandaloneExportResult.Success
    } catch (e: Throwable) {
        StandaloneExportResult.Failure(e.message ?: "Unknown error")
    }
}
