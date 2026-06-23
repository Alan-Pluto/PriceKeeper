package com.pricekeeper.app.data.ocr

/**
 * Stub OCR engine that returns an empty result.
 * Allows compilation and testing before the real ML Kit engine is wired.
 */
class OcrStubEngine : OcrEngine {

    override suspend fun recognize(imagePath: String): Result<String> {
        return Result.success("")
    }
}
