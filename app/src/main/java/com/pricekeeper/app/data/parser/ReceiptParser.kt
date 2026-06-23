package com.pricekeeper.app.data.parser

import com.pricekeeper.app.domain.model.ReceiptParseResult

/**
 * Strategy interface for parsing OCR raw text into structured receipt data.
 * Each implementation handles a specific receipt format.
 */
interface ReceiptParser {

    /**
     * Parse raw OCR text into a [ReceiptParseResult].
     * Implementations should be resilient to partial matches —
     * return what can be parsed, leave the rest.
     */
    fun parse(rawText: String): ReceiptParseResult
}
