package com.pricekeeper.app.domain.repository

import com.pricekeeper.app.domain.model.PriceRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository for price records — orchestrates multi-table operations
 * for adding a price record alongside goods and store lookups.
 */
interface PriceRecordRepository {

    /**
     * Add a price record, optionally finding or creating the associated
     * goods and store by name. Returns the new price record id.
     */
    suspend fun addPriceRecord(
        goodsName: String,
        storeName: String,
        price: Double,
        recordDate: Long = System.currentTimeMillis(),
        goodsCategory: String = "未分类",
        storeRegion: String = "",
        storeAddress: String? = null,
        storeLatitude: Double? = null,
        storeLongitude: Double? = null,
        storeMapUrl: String? = null,
        isPromotion: Boolean = false,
        note: String? = null,
        receiptId: Long? = null
    ): Long

    /** Observe all price records for a given goods item. */
    fun observePriceRecordsByGoods(goodsId: Long): Flow<List<PriceRecord>>

    /** Observe all price records for a given store. */
    fun observePriceRecordsByStore(storeId: Long): Flow<List<PriceRecord>>

    /** Get price records associated with a receipt. */
    suspend fun getPriceRecordsByReceipt(receiptId: Long): List<PriceRecord>

    /** Delete a single price record. */
    suspend fun deletePriceRecord(id: Long)

    /** Get total spending across all records. */
    suspend fun getTotalSpending(): Double

    /** Get total number of price records. */
    suspend fun getRecordCount(): Int
}
