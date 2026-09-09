package dev.lildua.oddly.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Hands the saved document to the system share sheet (spec §S22).
 *
 * The exported file is exactly what the app reads back on launch, so a user can
 * keep it anywhere they like and restore from it — no separate backup format to
 * keep in step with the storage format.
 */
internal object DataExport {

    private const val DIRECTORY = "exports"
    private const val FILE_NAME = "one-percent-human-backup.json"

    /** Returns a chooser for the exported file, or null if it could not be written. */
    fun chooserIntent(context: Context, json: String): Intent? {
        val uri = runCatching {
            val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
            val file = File(directory, FILE_NAME)
            file.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull() ?: return null

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
