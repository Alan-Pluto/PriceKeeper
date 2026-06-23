package com.pricekeeper.app.domain.model

/**
 * A single line item parsed from a receipt OCR result.
 */
data class ParsedReceiptItem(
    val name: String,
    val price: Double?
)
