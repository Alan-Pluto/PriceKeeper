package com.pricekeeper.app.domain.model

/**
 * Domain model for a receipt (小票).
 */
data class Receipt(
    val id: Long,
    val storeId: Long?,
    val totalPrice: Double?,
    val buyDate: Long,
    val imagePath: String,
    val ocrRawText: String?,
    val createdAt: Long
)
