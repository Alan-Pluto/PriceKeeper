package com.pricekeeper.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pricekeeper.app.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: ReceiptEntity): Long

    @Update
    suspend fun update(receipt: ReceiptEntity)

    @Delete
    suspend fun delete(receipt: ReceiptEntity)

    @Query("SELECT * FROM receipts ORDER BY buy_date DESC")
    fun observeAll(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE store_id = :storeId ORDER BY buy_date DESC")
    fun observeByStoreId(storeId: Long): Flow<List<ReceiptEntity>>

    @Query(
        """
        SELECT * FROM receipts
        WHERE buy_date BETWEEN :startDate AND :endDate
        ORDER BY buy_date DESC
        """
    )
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun count(): Int

    @Query("SELECT * FROM receipts")
    suspend fun getAllSync(): List<ReceiptEntity>

    @Query("DELETE FROM receipts")
    suspend fun deleteAll()
}
