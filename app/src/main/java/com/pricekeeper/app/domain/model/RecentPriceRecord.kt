package com.pricekeeper.app.domain.model

data class RecentPriceRecord(
    val id: Long,
    val goodsName: String,
    val storeName: String,
    val price: Double,
    val recordDate: Long
)
