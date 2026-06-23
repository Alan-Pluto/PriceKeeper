package com.pricekeeper.app.domain.model

/**
 * Aggregated price detail for a goods item.
 * Displayed on GoodsDetailScreen.
 */
data class GoodsPriceDetail(
    val goods: Goods,
    val lowestPrice: Double?,
    val highestPrice: Double?,
    val latestPrice: Double?,
    val trend: List<PricePoint>,
    val storePrices: List<StorePriceInfo>
)

/**
 * Price info from a specific store for comparison.
 */
data class StorePriceInfo(
    val storeName: String,
    val price: Double,
    val recordDate: Long
)
