package dev.lildua.oddly.ui.screens.settings

import android.content.Context
import android.net.Uri
import dev.lildua.oddly.data.local.OddlyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads a backup the user picked with the system file picker.
 *
 * The picker hands back a content Uri, so no storage permission is involved.
 */
internal object DataImport {

    /**
     * Some file managers and Drive tag a `.json` file as octet-stream or plain
     * text; a wrong file still fails safely when it is decoded.
     */
    val MIME_TYPES: Array<String> = arrayOf("application/json", "text/plain", "application/octet-stream")

    /**
     * The file's text, or null if it is unreadable or larger than
     * [OddlyRepository.MAX_BACKUP_BYTES]. Runs on IO: a Drive file may have to
     * be downloaded first, which would otherwise freeze the UI.
     */
    suspend fun readText(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val limit = OddlyRepository.MAX_BACKUP_BYTES
                val buffer = ByteArray(8 * 1024)
                val out = java.io.ByteArrayOutputStream()
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    out.write(buffer, 0, n)
                    // Stop early rather than read a wrong, huge file whole.
                    if (out.size() > limit) return@use null
                }
                out.toString(Charsets.UTF_8.name())
            }
        }.getOrNull()
    }
}
