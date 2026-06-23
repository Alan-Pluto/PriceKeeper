package com.pricekeeper.app.domain.repository

import com.pricekeeper.app.domain.model.Store
import com.pricekeeper.app.domain.model.StoreWithGoods
import kotlinx.coroutines.flow.Flow

/**
 * Repository for stores — the only entry point for store data operations.
 */
interface StoreRepository {

    /** Observe all stores, ordered by name. */
    fun observeStores(): Flow<List<Store>>

    /** Observe stores filtered by region. */
    fun observeStoresByRegion(region: String): Flow<List<Store>>

    /** Get a store with its tracked goods and prices. */
    suspend fun getStoreDetail(id: Long): StoreWithGoods?

    /** Create a new store. Returns the store id (existing or new). */
    suspend fun createStore(
        name: String,
        region: String,
        address: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Long

    /** Get all distinct region names. */
    fun observeAllRegions(): Flow<List<String>>

    /** Search stores by name substring. */
    fun searchStores(query: String): Flow<List<Store>>

    /** Get a single store by id. */
    suspend fun getStoreById(id: Long): Store?

    /** Update store metadata. */
    suspend fun updateStore(store: Store)

    /** Delete a store. Associated price records are cascade-deleted. */
    suspend fun deleteStore(id: Long)
}
