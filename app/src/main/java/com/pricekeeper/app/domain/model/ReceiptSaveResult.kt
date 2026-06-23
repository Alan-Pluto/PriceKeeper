package com.pricekeeper.app.domain.model

data class ReceiptSaveResult(
    val receiptId: Long,
    val savedItemCount: Int,
    val failedItems: List<String>
)
