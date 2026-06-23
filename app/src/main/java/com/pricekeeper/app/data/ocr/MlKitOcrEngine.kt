package com.pricekeeper.app.data.ocr

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device OCR engine using Google ML Kit Chinese text recognition.
 * Operates entirely offline — no network required.
 */
@Singleton
class MlKitOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    override suspend fun recognize(imagePath: String): Result<String> {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                return Result.failure(OcrException.ImageNotFound(imagePath))
            }

            val inputImage = InputImage.fromFilePath(context, android.net.Uri.fromFile(file))
            val task = recognizer.process(inputImage)
            val visionText = Tasks.await(task)
            val text = visionText.text

            if (text.isNullOrBlank()) {
                return Result.failure(OcrException.EmptyResult())
            }

            Result.success(text)
        } catch (e: OcrException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(OcrException.TextDetectionFailed(e))
        }
    }

    fun close() {
        try {
            recognizer.close()
        } catch (_: Exception) { }
    }
}
