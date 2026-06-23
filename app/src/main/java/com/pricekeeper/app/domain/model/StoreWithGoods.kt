package com.pricekeeper.app.domain.model

/**
 * A store with its tracked goods and their current prices.
 * Used on StoreDetailScreen.
 */
data class StoreWithGoods(
    val store: Store,
    val goodsSummaries: List<StoreGoodsItem>
)

/**
 * Summary of a goods item within a store context.
 */
data class StoreGoodsItem(
    val goodsId: Long,
    val goodsName: String,
    val minPrice: Double,
    val maxPrice: Double,
    val lastBuyDate: Long?
)
