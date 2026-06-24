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
    val storeId: Long,
    val storeName: String,
    val storeAddress: String?,
    val storeLatitude: Double?,
    val storeLongitude: Double?,
    val storeMapUrl: String?,
    val price: Double,
    val recordDate: Long
)
