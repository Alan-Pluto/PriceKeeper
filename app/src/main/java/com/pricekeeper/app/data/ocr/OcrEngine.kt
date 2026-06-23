package com.pricekeeper.app.data.ocr

/**
 * Abstraction for OCR engines. Implementations may use ML Kit, Tesseract,
 * or cloud services — all return raw text that is then parsed downstream.
 *
 * Defined in the data layer because it is a data-source concern
 * (converting an image to text, not business logic).
 */
interface OcrEngine {

    /**
     * Recognize text from an image at [imagePath].
     * Returns a [Result] to cleanly propagate failures without exceptions.
     */
    suspend fun recognize(imagePath: String): Result<String>
}
