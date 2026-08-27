package dev.lildua.oddly.ui.screens.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import dev.lildua.oddly.ui.theme.OddlyColors
import java.io.File

/**
 * Local image generation for the share card (spec §S18, Phase 4).
 *
 * The card is rendered to a bitmap, written to the app's own cache, and handed
 * to the system chooser through a [FileProvider]. Nothing leaves the device
 * except what the user explicitly picks a target for, and the file only ever
 * contains the aggregate achievements the card shows.
 *
 * The iOS half of this is `ImageRenderer` + `ShareLink` in `ShareCardScreen`.
 */
internal object ShareImageExport {

    private const val DIRECTORY = "images"
    private const val FILE_NAME = "one-percent-human-share.png"

    /**
     * Writes [bitmap] to the cache and returns a chooser intent for it, or null
     * if the file could not be written — a full disk should not crash the app.
     */
    fun chooserIntent(context: Context, bitmap: Bitmap, caption: String): Intent? {
        val uri = write(context, bitmap) ?: return null

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun write(context: Context, bitmap: Bitmap): android.net.Uri? = runCatching {
        val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
        val file = File(directory, FILE_NAME)
        file.outputStream().use { flatten(bitmap).compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()

    /**
     * Composites the captured card onto the app background.
     *
     * A captured layer keeps whatever translucency the composition had, and the
     * card's rounded corners are transparent. Shared as-is the image renders
     * against whatever the receiving app puts behind it — white, in most chat
     * apps — which washes the whole card out. Flattening first makes what the
     * recipient sees identical to what the sender previewed.
     */
    private fun flatten(source: Bitmap): Bitmap {
        // `toImageBitmap()` can hand back a hardware-backed bitmap, which a
        // software Canvas refuses to touch — copy into a mutable software one
        // first, then lay the background *under* what was captured.
        val flattened = source.copy(Bitmap.Config.ARGB_8888, true) ?: return source
        Canvas(flattened).drawColor(OddlyColors.Background.toArgb(), PorterDuff.Mode.DST_OVER)
        return flattened
    }
}
