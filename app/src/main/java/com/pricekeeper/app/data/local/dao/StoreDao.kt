package com.pricekeeper.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pricekeeper.app.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(store: StoreEntity): Long

    @Update
    suspend fun update(store: StoreEntity)

    @Delete
    suspend fun delete(store: StoreEntity)

    @Query("SELECT * FROM store ORDER BY name ASC")
    fun observeAll(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM store WHERE id = :id")
    suspend fun getById(id: Long): StoreEntity?

    @Query("SELECT * FROM store WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): StoreEntity?

    @Query("SELECT * FROM store WHERE region = :region ORDER BY name ASC")
    fun observeByRegion(region: String): Flow<List<StoreEntity>>

    @Query("SELECT * FROM store WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<StoreEntity>>

    @Query("SELECT DISTINCT region FROM store ORDER BY region ASC")
    fun observeAllRegions(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM store")
    suspend fun count(): Int

    @Query("SELECT * FROM store")
    suspend fun getAllSync(): List<StoreEntity>

    @Query("DELETE FROM store")
    suspend fun deleteAll()
}
