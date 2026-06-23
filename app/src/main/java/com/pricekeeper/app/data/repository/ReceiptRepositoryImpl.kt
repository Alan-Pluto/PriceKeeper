package com.pricekeeper.app.data.repository

import com.pricekeeper.app.data.local.dao.ReceiptDao
import com.pricekeeper.app.data.local.entity.ReceiptEntity
import com.pricekeeper.app.data.mapper.toDomain
import com.pricekeeper.app.domain.model.Receipt
import com.pricekeeper.app.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    private val receiptDao: ReceiptDao
) : ReceiptRepository {

    override fun observeReceipts(): Flow<List<Receipt>> {
        return receiptDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeReceiptsByStore(storeId: Long): Flow<List<Receipt>> {
        return receiptDao.observeByStoreId(storeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReceiptById(id: Long): Receipt? {
        return receiptDao.getById(id)?.toDomain()
    }

    override suspend fun saveReceipt(
        storeId: Long?,
        totalPrice: Double?,
        buyDate: Long,
        imagePath: String,
        ocrRawText: String?
    ): Long {
        val entity = ReceiptEntity(
            storeId = storeId,
            totalPrice = totalPrice,
            buyDate = buyDate,
            imagePath = imagePath,
            ocrRawText = ocrRawText
        )
        return receiptDao.insert(entity)
    }

    override suspend fun deleteReceipt(id: Long) {
        receiptDao.getById(id)?.let { receiptDao.delete(it) }
    }

    override suspend fun getReceiptCount(): Int {
        return receiptDao.count()
    }
}
