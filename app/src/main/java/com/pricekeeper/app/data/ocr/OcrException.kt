package com.pricekeeper.app.data.ocr

/**
 * Exception hierarchy for OCR failures.
 */
sealed class OcrException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ImageNotFound(path: String) : OcrException("Image file not found: $path")
    class TextDetectionFailed(cause: Throwable?) : OcrException("Text detection failed", cause)
    class EmptyResult : OcrException("OCR returned empty result — no text detected in image")
}
