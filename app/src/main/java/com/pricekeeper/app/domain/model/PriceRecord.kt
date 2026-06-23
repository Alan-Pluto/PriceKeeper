package com.pricekeeper.app.domain.model

/**
 * Domain model for a single price record.
 */
data class PriceRecord(
    val id: Long,
    val goodsId: Long,
    val storeId: Long,
    val price: Double,
    val recordDate: Long,
    val receiptId: Long?,
    val isPromotion: Boolean,
    val note: String?,
    val createdAt: Long
)
