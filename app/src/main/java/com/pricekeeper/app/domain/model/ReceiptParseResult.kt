package com.pricekeeper.app.domain.model

/**
 * Result of parsing OCR raw text from a receipt.
 */
data class ReceiptParseResult(
    val items: List<ParsedReceiptItem>,
    val totalPrice: Double?,
    val storeNameHint: String?,
    val parserName: String = "DefaultParser"
) {
    /** Number of items successfully parsed with a price. */
    val pricedItemCount: Int get() = items.count { it.price != null }

    /** Items that have a valid price. */
    val pricedItems: List<ParsedReceiptItem> get() = items.filter { it.price != null }
}
