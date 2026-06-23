package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.repository.PriceRecordRepository
import javax.inject.Inject

/**
 * Orchestrates adding a manual price record. Handles find-or-create
 * for both goods and store, then inserts the price record.
 */
class AddManualPriceRecordUseCase @Inject constructor(
    private val priceRecordRepository: PriceRecordRepository
) {
    suspend operator fun invoke(
        goodsName: String,
        storeName: String,
        price: Double,
        recordDate: Long = System.currentTimeMillis(),
        goodsCategory: String = "未分类",
        storeRegion: String = "",
        storeAddress: String? = null,
        storeLatitude: Double? = null,
        storeLongitude: Double? = null,
        storeMapUrl: String? = null,
        isPromotion: Boolean = false,
        note: String? = null
    ): Result<Long> {
        return try {
            require(goodsName.isNotBlank()) { "商品名称不能为空" }
            require(price > 0) { "价格必须大于0" }
            require(storeName.isNotBlank()) { "商店名称不能为空" }

            val recordId = priceRecordRepository.addPriceRecord(
                goodsName = goodsName.trim(),
                storeName = storeName.trim(),
                price = price,
                recordDate = recordDate,
                goodsCategory = goodsCategory,
                storeRegion = storeRegion,
                storeAddress = storeAddress?.trim(),
                storeLatitude = storeLatitude,
                storeLongitude = storeLongitude,
                storeMapUrl = storeMapUrl?.trim(),
                isPromotion = isPromotion,
                note = note?.trim()
            )
            Result.success(recordId)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
