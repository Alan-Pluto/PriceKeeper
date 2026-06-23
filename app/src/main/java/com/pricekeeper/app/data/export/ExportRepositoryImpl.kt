package com.pricekeeper.app.data.export

import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.ReceiptDao
import com.pricekeeper.app.data.local.dao.StoreDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl @Inject constructor(
    private val goodsDao: GoodsDao,
    private val storeDao: StoreDao,
    private val priceRecordDao: PriceRecordDao,
    private val receiptDao: ReceiptDao,
    private val json: Json
) : ExportRepository {

    override suspend fun exportAllData(outputStream: OutputStream): ExportStats =
        withContext(Dispatchers.IO) {
            // Read all data (suspend queries for one-time reads)
            val goods = goodsDao.getAllSync()
            val stores = storeDao.getAllSync()
            val priceRecords = priceRecordDao.getAllSync()
            val receipts = receiptDao.getAllSync()

            val bundle = ExportBundle(
                exportedAt = System.currentTimeMillis(),
                goods = goods.map { it.toExport() },
                stores = stores.map { it.toExport() },
                priceRecords = priceRecords.map { it.toExport() },
                receipts = receipts.map { it.toExport() }
            )

            val jsonString = json.encodeToString(ExportBundle.serializer(), bundle)

            GZIPOutputStream(outputStream).use { gzip ->
                gzip.write(jsonString.toByteArray(Charsets.UTF_8))
                gzip.finish()
            }

            ExportStats(
                goodsCount = goods.size,
                storeCount = stores.size,
                priceRecordCount = priceRecords.size,
                receiptCount = receipts.size,
                fileSizeBytes = jsonString.toByteArray(Charsets.UTF_8).size.toLong()
            )
        }

    // Mappers for export DTOs
    private fun com.pricekeeper.app.data.local.entity.GoodsEntity.toExport() = ExportGoods(
        id = id, name = name, category = category, specUnit = specUnit,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun com.pricekeeper.app.data.local.entity.StoreEntity.toExport() = ExportStore(
        id = id, name = name, region = region, address = address,
        latitude = latitude, longitude = longitude, mapUrl = mapUrl, myNote = myNote,
        rating = rating, createdAt = createdAt
    )

    private fun com.pricekeeper.app.data.local.entity.PriceRecordEntity.toExport() = ExportPriceRecord(
        id = id, goodsId = goodsId, storeId = storeId, price = price,
        recordDate = recordDate, receiptId = receiptId, isPromotion = isPromotion,
        note = note, createdAt = createdAt
    )

    private fun com.pricekeeper.app.data.local.entity.ReceiptEntity.toExport() = ExportReceipt(
        id = id, storeId = storeId, totalPrice = totalPrice, buyDate = buyDate,
        imagePath = imagePath, ocrRawText = ocrRawText, createdAt = createdAt
    )
}
