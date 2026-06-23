package com.pricekeeper.app.data.repository

import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import com.pricekeeper.app.data.local.entity.StoreEntity
import com.pricekeeper.app.data.mapper.toDomain
import com.pricekeeper.app.domain.model.PriceRecord
import com.pricekeeper.app.domain.repository.PriceRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRecordRepositoryImpl @Inject constructor(
    private val priceRecordDao: PriceRecordDao,
    private val goodsDao: GoodsDao,
    private val storeDao: StoreDao
) : PriceRecordRepository {

    override suspend fun addPriceRecord(
        goodsName: String,
        storeName: String,
        price: Double,
        recordDate: Long,
        goodsCategory: String,
        storeRegion: String,
        storeAddress: String?,
        storeLatitude: Double?,
        storeLongitude: Double?,
        storeMapUrl: String?,
        isPromotion: Boolean,
        note: String?,
        receiptId: Long?
    ): Long {
        // Find or create goods
        val goodsId = findOrCreateGoods(goodsName, goodsCategory)

        // Find or create store
        val storeId = findOrCreateStore(
            name = storeName,
            region = storeRegion,
            address = storeAddress,
            latitude = storeLatitude,
            longitude = storeLongitude,
            mapUrl = storeMapUrl
        )

        val record = PriceRecordEntity(
            goodsId = goodsId,
            storeId = storeId,
            price = price,
            recordDate = recordDate,
            receiptId = receiptId,
            isPromotion = isPromotion,
            note = note
        )
        return priceRecordDao.insert(record)
    }

    private suspend fun findOrCreateGoods(name: String, category: String): Long {
        val existing = goodsDao.getByName(name)
        if (existing != null) return existing.id
        val entity = GoodsEntity(name = name, category = category)
        val id = goodsDao.insert(entity)
        return if (id > 0) id else goodsDao.getByName(name)!!.id
    }

    private suspend fun findOrCreateStore(
        name: String,
        region: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        mapUrl: String?
    ): Long {
        val existing = storeDao.getByName(name)
        if (existing != null) {
            val shouldBackfillLocation =
                existing.latitude == null && latitude != null && longitude != null
            val shouldBackfillAddress =
                existing.address.isNullOrBlank() && !address.isNullOrBlank()
            val shouldBackfillRegion =
                existing.region.isBlank() && region.isNotBlank()
            val shouldBackfillMapUrl =
                existing.mapUrl.isNullOrBlank() && !mapUrl.isNullOrBlank()
            if (shouldBackfillLocation || shouldBackfillAddress || shouldBackfillRegion || shouldBackfillMapUrl) {
                storeDao.update(
                    existing.copy(
                        region = if (shouldBackfillRegion) region else existing.region,
                        address = if (shouldBackfillAddress) address else existing.address,
                        latitude = if (shouldBackfillLocation) latitude else existing.latitude,
                        longitude = if (shouldBackfillLocation) longitude else existing.longitude,
                        mapUrl = if (shouldBackfillMapUrl) mapUrl else existing.mapUrl
                    )
                )
            }
            return existing.id
        }
        val entity = StoreEntity(
            name = name,
            region = region,
            address = address,
            latitude = latitude,
            longitude = longitude,
            mapUrl = mapUrl
        )
        val id = storeDao.insert(entity)
        return if (id > 0) id else storeDao.getByName(name)!!.id
    }

    override fun observePriceRecordsByGoods(goodsId: Long): Flow<List<PriceRecord>> {
        return priceRecordDao.observeByGoodsId(goodsId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePriceRecordsByStore(storeId: Long): Flow<List<PriceRecord>> {
        return priceRecordDao.observeByStoreId(storeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPriceRecordsByReceipt(receiptId: Long): List<PriceRecord> {
        return priceRecordDao.getByReceiptId(receiptId).map { it.toDomain() }
    }

    override suspend fun deletePriceRecord(id: Long) {
        priceRecordDao.getById(id)?.let { priceRecordDao.delete(it) }
    }

    override suspend fun getTotalSpending(): Double {
        return priceRecordDao.getTotalSpending() ?: 0.0
    }

    override suspend fun getRecordCount(): Int {
        return priceRecordDao.count()
    }
}
