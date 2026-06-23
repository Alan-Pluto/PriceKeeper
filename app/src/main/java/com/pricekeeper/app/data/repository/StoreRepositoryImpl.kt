package com.pricekeeper.app.data.repository

import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.entity.StoreEntity
import com.pricekeeper.app.data.mapper.toDomain
import com.pricekeeper.app.data.mapper.toEntity
import com.pricekeeper.app.domain.model.Store
import com.pricekeeper.app.domain.model.StoreGoodsItem
import com.pricekeeper.app.domain.model.StoreWithGoods
import com.pricekeeper.app.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val storeDao: StoreDao,
    private val priceRecordDao: PriceRecordDao
) : StoreRepository {

    override fun observeStores(): Flow<List<Store>> {
        return storeDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeStoresByRegion(region: String): Flow<List<Store>> {
        return storeDao.observeByRegion(region).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreDetail(id: Long): StoreWithGoods? {
        val storeEntity = storeDao.getById(id) ?: return null
        val summaries = priceRecordDao.getStoreGoodsSummaries(id).map {
            StoreGoodsItem(
                goodsId = it.goodsId,
                goodsName = it.goodsName,
                minPrice = it.minPrice,
                maxPrice = it.maxPrice,
                lastBuyDate = it.lastBuyDate
            )
        }
        return StoreWithGoods(
            store = storeEntity.toDomain(),
            goodsSummaries = summaries
        )
    }

    override suspend fun createStore(
        name: String,
        region: String,
        address: String?,
        latitude: Double?,
        longitude: Double?
    ): Long {
        val entity = StoreEntity(
            name = name,
            region = region,
            address = address,
            latitude = latitude,
            longitude = longitude
        )
        val insertedId = storeDao.insert(entity)
        return if (insertedId > 0) insertedId
        else storeDao.getByName(name)?.id ?: throw IllegalStateException("Failed to create store")
    }

    override fun observeAllRegions(): Flow<List<String>> {
        return storeDao.observeAllRegions()
    }

    override fun searchStores(query: String): Flow<List<Store>> {
        return storeDao.searchByName(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreById(id: Long): Store? {
        return storeDao.getById(id)?.toDomain()
    }

    override suspend fun updateStore(store: Store) {
        storeDao.update(store.toEntity())
    }

    override suspend fun deleteStore(id: Long) {
        storeDao.getById(id)?.let { storeDao.delete(it) }
    }
}
