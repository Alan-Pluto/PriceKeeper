package com.pricekeeper.app.data.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Compresses receipt images to a max width for efficient OCR processing and storage.
 */
@Singleton
class ImageCompressor @Inject constructor() {

    companion object {
        const val MAX_WIDTH_PX = 1080
        const val JPEG_QUALITY = 85
    }

    /**
     * Compress image at [sourcePath] and write to [destPath].
     * Scales down to [maxWidth] px width, maintaining aspect ratio.
     * Returns the destination file.
     */
    suspend fun compress(sourcePath: String, destPath: String, maxWidth: Int = MAX_WIDTH_PX): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(sourcePath, options)

        // Calculate sample size for efficient loading
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(sourcePath, options)
            ?: throw OcrException.ImageNotFound(sourcePath)

        val scaledWidth: Int
        val scaledHeight: Int
        if (bitmap.width > maxWidth) {
            scaledWidth = maxWidth
            scaledHeight = (bitmap.height.toFloat() / bitmap.width * maxWidth).toInt()
        } else {
            scaledWidth = bitmap.width
            scaledHeight = bitmap.height
        }

        val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        if (scaled !== bitmap) bitmap.recycle()

        val dest = File(destPath)
        FileOutputStream(dest).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        scaled.recycle()

        return dest
    }

    private fun calculateSampleSize(width: Int, height: Int, maxWidth: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > maxWidth * 2) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
