package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.ParsedReceiptItem
import com.pricekeeper.app.domain.model.ReceiptSaveResult
import com.pricekeeper.app.domain.repository.PriceRecordRepository
import com.pricekeeper.app.domain.repository.ReceiptRepository
import javax.inject.Inject

/**
 * Orchestrates saving a recognized receipt to the database.
 * Writes Receipt → then each item as a PriceRecord, all linked by receiptId.
 */
class RecognizeAndSaveReceiptUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val priceRecordRepository: PriceRecordRepository
) {
    suspend operator fun invoke(
        imagePath: String,
        ocrRawText: String,
        items: List<ParsedReceiptItem>,
        storeName: String,
        storeRegion: String = "",
        buyDate: Long = System.currentTimeMillis(),
        totalPrice: Double? = null
    ): Result<ReceiptSaveResult> {
        return try {
            require(items.isNotEmpty()) { "没有可保存的商品项" }
            require(storeName.isNotBlank()) { "请选择或输入商店名称" }

            // Calculate total from items if not provided
            val calcTotal = totalPrice ?: items.sumOf { it.price ?: 0.0 }

            // 1. Save the receipt
            val receiptId = receiptRepository.saveReceipt(
                storeId = null, // We'll find/create store per item
                totalPrice = calcTotal,
                buyDate = buyDate,
                imagePath = imagePath,
                ocrRawText = ocrRawText
            )

            // 2. Save each item as a price record
            var savedCount = 0
            val failedItems = mutableListOf<String>()

            for (item in items) {
                try {
                    val price = item.price
                    if (price != null && price > 0) {
                        priceRecordRepository.addPriceRecord(
                            goodsName = item.name,
                            storeName = storeName,
                            price = price,
                            recordDate = buyDate,
                            storeRegion = storeRegion,
                            storeAddress = null,
                            storeLatitude = null,
                            storeLongitude = null,
                            storeMapUrl = null,
                            receiptId = receiptId
                        )
                        savedCount++
                    } else {
                        failedItems.add(item.name)
                    }
                } catch (e: Exception) {
                    failedItems.add("${item.name}: ${e.message}")
                }
            }

            Result.success(
                ReceiptSaveResult(
                    receiptId = receiptId,
                    savedItemCount = savedCount,
                    failedItems = failedItems
                )
            )
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
