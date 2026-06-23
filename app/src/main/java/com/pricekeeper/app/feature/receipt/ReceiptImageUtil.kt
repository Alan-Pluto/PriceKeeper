package com.pricekeeper.app.feature.receipt

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Utility for receipt image handling. */
object ReceiptImageUtil {

    /**
     * Create an empty temporary file for captured receipt images.
     * Saved in the app's cache directory.
     */
    fun createTempImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = File(context.cacheDir, "receipts")
        dir.mkdirs()
        return File(dir, "receipt_$timestamp.jpg")
    }

    /**
     * Copy a content URI to a local file for OCR processing.
     * Returns the local file path.
     */
    fun copyUriToFile(context: Context, uri: Uri, dest: File): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open URI: $uri")
        return dest.absolutePath
    }
}
