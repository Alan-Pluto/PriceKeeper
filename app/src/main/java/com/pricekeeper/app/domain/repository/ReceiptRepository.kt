package com.pricekeeper.app.domain.repository

import com.pricekeeper.app.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

/**
 * Repository for receipts — manages receipt image storage and OCR workflow.
 */
interface ReceiptRepository {

    /** Observe all receipts, ordered by purchase date descending. */
    fun observeReceipts(): Flow<List<Receipt>>

    /** Observe receipts for a specific store. */
    fun observeReceiptsByStore(storeId: Long): Flow<List<Receipt>>

    /** Get a single receipt by id. */
    suspend fun getReceiptById(id: Long): Receipt?

    /**
     * Save a receipt image and its OCR result.
     * Returns the new receipt id.
     */
    suspend fun saveReceipt(
        storeId: Long?,
        totalPrice: Double?,
        buyDate: Long,
        imagePath: String,
        ocrRawText: String?
    ): Long

    /** Delete a receipt. */
    suspend fun deleteReceipt(id: Long)

    /** Get total number of receipts. */
    suspend fun getReceiptCount(): Int
}
