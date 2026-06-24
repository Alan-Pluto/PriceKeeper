package com.pricekeeper.app.data.repository

import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.data.mapper.toDomain
import com.pricekeeper.app.domain.model.Goods
import com.pricekeeper.app.domain.model.GoodsPriceDetail
import com.pricekeeper.app.domain.model.PricePoint
import com.pricekeeper.app.domain.model.StorePriceInfo
import com.pricekeeper.app.domain.repository.GoodsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoodsRepositoryImpl @Inject constructor(
    private val goodsDao: GoodsDao,
    private val priceRecordDao: PriceRecordDao
) : GoodsRepository {

    override fun observeGoods(): Flow<List<Goods>> {
        return goodsDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeGoodsMinPrices(): Flow<Map<Long, Double>> {
        return priceRecordDao.observeAllGoodsMinPrices().map { prices ->
            prices.associate { it.goodsId to it.minPrice }
        }
    }

    override fun observeGoodsByCategory(category: String): Flow<List<Goods>> {
        return goodsDao.observeByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGoodsDetail(id: Long): GoodsPriceDetail? {
        val goodsEntity = goodsDao.getById(id) ?: return null
        val aggregate = priceRecordDao.getGoodsPriceAggregate(id)
        val trend = priceRecordDao.getPriceTrend(id).map {
            PricePoint(timestamp = it.recordDate, price = it.price)
        }
        val storePrices = priceRecordDao.getStorePrices(id).map {
            StorePriceInfo(
                storeId = it.storeId,
                storeName = it.storeName,
                storeAddress = it.storeAddress,
                storeLatitude = it.storeLatitude,
                storeLongitude = it.storeLongitude,
                storeMapUrl = it.storeMapUrl,
                price = it.price,
                recordDate = it.recordDate
            )
        }

        return GoodsPriceDetail(
            goods = goodsEntity.toDomain(),
            lowestPrice = aggregate?.minPrice,
            highestPrice = aggregate?.maxPrice,
            latestPrice = aggregate?.latestPrice,
            trend = trend,
            storePrices = storePrices
        )
    }

    override suspend fun createGoods(
        name: String,
        category: String,
        specUnit: String?
    ): Long {
        val entity = GoodsEntity(
            name = name,
            category = category,
            specUnit = specUnit
        )
        val insertedId = goodsDao.insert(entity)
        // insert returns -1 on conflict (IGNORE strategy), so fetch existing
        return if (insertedId > 0) insertedId
        else goodsDao.getByName(name)?.id ?: throw IllegalStateException("Failed to create goods")
    }

    override fun observeAllCategories(): Flow<List<String>> {
        return goodsDao.observeAllCategories()
    }

    override suspend fun renameCategory(oldName: String, newName: String) {
        goodsDao.renameCategory(oldName = oldName, newName = newName)
    }

    override suspend fun moveCategory(category: String, targetName: String) {
        goodsDao.moveCategory(category = category, targetName = targetName)
    }

    override fun searchGoods(query: String): Flow<List<Goods>> {
        return goodsDao.searchByName(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGoodsById(id: Long): Goods? {
        return goodsDao.getById(id)?.toDomain()
    }

    override suspend fun deleteGoods(id: Long) {
        goodsDao.getById(id)?.let { goodsDao.delete(it) }
    }
}
