package com.pricekeeper.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Intermediate result for goods price aggregation (TDD §5.1).
 */
data class GoodsPriceAggregate(
    val minPrice: Double?,
    val maxPrice: Double?,
    val latestPrice: Double?
)

/**
 * Intermediate result for store price comparison (TDD §5.3).
 */
data class StorePriceSummary(
    val storeId: Long,
    val storeName: String,
    val storeAddress: String?,
    val storeLatitude: Double?,
    val storeLongitude: Double?,
    val storeMapUrl: String?,
    val price: Double,
    val recordDate: Long
)

/**
 * Intermediate result for store-goods aggregation (TDD §5.4).
 */
data class StoreGoodsSummary(
    val goodsId: Long,
    val goodsName: String,
    val minPrice: Double,
    val maxPrice: Double,
    val lastBuyDate: Long?
)

/**
 * Lightweight home-feed row. Keep this intentionally small so the Home page
 * can render recent manual entries without loading full goods/store details.
 */
data class RecentPriceRecordSummary(
    val id: Long,
    val goodsName: String,
    val storeName: String,
    val price: Double,
    val recordDate: Long
)

@Dao
interface PriceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PriceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PriceRecordEntity>): List<Long>

    @Delete
    suspend fun delete(record: PriceRecordEntity)

    // ---- TDD §5.1: Goods detail aggregate (min, max, latest price) ----
    @Query(
        """
        SELECT
            MIN(price) AS minPrice,
            MAX(price) AS maxPrice,
            (SELECT price
             FROM price_records
             WHERE goods_id = :goodsId
             ORDER BY record_date DESC
             LIMIT 1
            ) AS latestPrice
        FROM price_records
        WHERE goods_id = :goodsId
        """
    )
    suspend fun getGoodsPriceAggregate(goodsId: Long): GoodsPriceAggregate?

    // ---- TDD §5.2: Price trend (date, price) ----
    @Query(
        """
        SELECT record_date AS recordDate, price
        FROM price_records
        WHERE goods_id = :goodsId
        ORDER BY record_date ASC
        """
    )
    suspend fun getPriceTrend(goodsId: Long): List<PricePointResult>

    // ---- TDD §5.3: Store price comparison ----
    @Query(
        """
        SELECT
            s.id AS storeId,
            s.name AS storeName,
            s.address AS storeAddress,
            s.latitude AS storeLatitude,
            s.longitude AS storeLongitude,
            s.map_url AS storeMapUrl,
            pr.price,
            pr.record_date AS recordDate
        FROM price_records pr
        JOIN store s ON pr.store_id = s.id
        WHERE pr.goods_id = :goodsId
        ORDER BY pr.price ASC
        """
    )
    suspend fun getStorePrices(goodsId: Long): List<StorePriceSummary>

    // ---- TDD §5.4: Store-goods aggregation ----
    @Query(
        """
        SELECT
            g.id AS goodsId,
            g.name AS goodsName,
            MIN(pr.price) AS minPrice,
            MAX(pr.price) AS maxPrice,
            MAX(pr.record_date) AS lastBuyDate
        FROM price_records pr
        JOIN goods g ON pr.goods_id = g.id
        WHERE pr.store_id = :storeId
        GROUP BY g.id
        """
    )
    suspend fun getStoreGoodsSummaries(storeId: Long): List<StoreGoodsSummary>

    // ---- TDD §5.5: Dashboard stats ----
    @Query("SELECT SUM(price) FROM price_records")
    suspend fun getTotalSpending(): Double?

    @Query("SELECT COUNT(*) FROM price_records")
    suspend fun count(): Int

    @Query("SELECT * FROM price_records")
    suspend fun getAllSync(): List<PriceRecordEntity>

    @Query("DELETE FROM price_records")
    suspend fun deleteAll()

    // ---- Basic CRUD queries ----
    @Query("SELECT goods_id AS goodsId, MIN(price) AS minPrice FROM price_records GROUP BY goods_id")
    suspend fun getAllGoodsMinPrices(): List<GoodsMinPrice>

    @Query("SELECT goods_id AS goodsId, MIN(price) AS minPrice FROM price_records GROUP BY goods_id")
    fun observeAllGoodsMinPrices(): Flow<List<GoodsMinPrice>>

    @Query("SELECT * FROM price_records WHERE id = :id")
    suspend fun getById(id: Long): PriceRecordEntity?

    @Query("SELECT * FROM price_records WHERE goods_id = :goodsId ORDER BY record_date DESC")
    fun observeByGoodsId(goodsId: Long): Flow<List<PriceRecordEntity>>

    @Query("SELECT * FROM price_records WHERE store_id = :storeId ORDER BY record_date DESC")
    fun observeByStoreId(storeId: Long): Flow<List<PriceRecordEntity>>

    @Query(
        """
        SELECT
            pr.id AS id,
            g.name AS goodsName,
            s.name AS storeName,
            pr.price AS price,
            pr.record_date AS recordDate
        FROM price_records pr
        JOIN goods g ON pr.goods_id = g.id
        JOIN store s ON pr.store_id = s.id
        ORDER BY pr.record_date DESC, pr.id DESC
        LIMIT :limit
        """
    )
    fun observeRecentRecords(limit: Int): Flow<List<RecentPriceRecordSummary>>

}

/**
 * Intermediate result for all-goods min price query.
 */
data class GoodsMinPrice(
    val goodsId: Long,
    val minPrice: Double
)

/**
 * Intermediate result for price trend query (TDD §5.2).
 */
data class PricePointResult(
    val recordDate: Long,
    val price: Double
)
