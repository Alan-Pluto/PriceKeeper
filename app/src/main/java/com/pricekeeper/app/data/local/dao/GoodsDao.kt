package com.pricekeeper.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pricekeeper.app.data.local.entity.GoodsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoodsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(goods: GoodsEntity): Long

    @Update
    suspend fun update(goods: GoodsEntity)

    @Delete
    suspend fun delete(goods: GoodsEntity)

    @Query("SELECT * FROM goods ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<GoodsEntity>>

    @Query("SELECT * FROM goods WHERE id = :id")
    suspend fun getById(id: Long): GoodsEntity?

    @Query("SELECT * FROM goods WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): GoodsEntity?

    @Query("SELECT * FROM goods WHERE category = :category ORDER BY updated_at DESC")
    fun observeByCategory(category: String): Flow<List<GoodsEntity>>

    @Query("SELECT * FROM goods WHERE name LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun searchByName(query: String): Flow<List<GoodsEntity>>

    @Query("SELECT DISTINCT category FROM goods ORDER BY category ASC")
    fun observeAllCategories(): Flow<List<String>>

    @Query("UPDATE goods SET category = :newName, updated_at = :updatedAt WHERE category = :oldName")
    suspend fun renameCategory(oldName: String, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE goods SET category = :targetName, updated_at = :updatedAt WHERE category = :category")
    suspend fun moveCategory(category: String, targetName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM goods")
    suspend fun count(): Int

    @Query("SELECT * FROM goods")
    suspend fun getAllSync(): List<GoodsEntity>

    @Query("DELETE FROM goods")
    suspend fun deleteAll()
}
