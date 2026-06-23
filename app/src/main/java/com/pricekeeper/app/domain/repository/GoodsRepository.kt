package com.pricekeeper.app.domain.repository

import com.pricekeeper.app.domain.model.Goods
import com.pricekeeper.app.domain.model.GoodsPriceDetail
import kotlinx.coroutines.flow.Flow

/**
 * Repository for goods — the only entry point for goods data operations.
 * ViewModel must use this interface, never DAO directly.
 */
interface GoodsRepository {

    /** Observe all goods, ordered by most recently updated. */
    fun observeGoods(): Flow<List<Goods>>

    /** Observe the current lowest price for each goods item. */
    fun observeGoodsMinPrices(): Flow<Map<Long, Double>>

    /** Observe goods filtered by category. */
    fun observeGoodsByCategory(category: String): Flow<List<Goods>>

    /** Get aggregated price detail for a goods item. */
    suspend fun getGoodsDetail(id: Long): GoodsPriceDetail?

    /** Create a new goods item. Returns the new goods id. */
    suspend fun createGoods(name: String, category: String, specUnit: String? = null): Long

    /** Get all distinct category names. */
    fun observeAllCategories(): Flow<List<String>>

    /** Rename all goods currently assigned to a category. */
    suspend fun renameCategory(oldName: String, newName: String)

    /** Move all goods in one category to another category. */
    suspend fun moveCategory(category: String, targetName: String = "未分类")

    /** Search goods by name substring. */
    fun searchGoods(query: String): Flow<List<Goods>>

    /** Get a single goods item by id. */
    suspend fun getGoodsById(id: Long): Goods?

    /** Delete a goods item and all its price records (cascade). */
    suspend fun deleteGoods(id: Long)
}
