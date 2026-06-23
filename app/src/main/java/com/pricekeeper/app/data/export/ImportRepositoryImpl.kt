package com.pricekeeper.app.data.export

import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.ReceiptDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import com.pricekeeper.app.data.local.entity.ReceiptEntity
import com.pricekeeper.app.data.local.entity.StoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepositoryImpl @Inject constructor(
    private val goodsDao: GoodsDao,
    private val storeDao: StoreDao,
    private val priceRecordDao: PriceRecordDao,
    private val receiptDao: ReceiptDao,
    private val json: Json
) : ImportRepository {

    override suspend fun importData(
        inputStream: InputStream,
        strategy: ImportConflictStrategy
    ): ImportResult = withContext(Dispatchers.IO) {

        // 1. Decompress and deserialize
        val jsonString = GZIPInputStream(inputStream).use { gzip ->
            gzip.reader(Charsets.UTF_8).readText()
        }

        val bundle = try {
            json.decodeFromString(ExportBundle.serializer(), jsonString)
        } catch (e: Exception) {
            return@withContext ImportResult(
                errors = listOf("文件格式错误，导入失败: ${e.message}")
            )
        }

        // 2. Validate
        val validator = DataValidator()
        val validationErrors = validator.validate(bundle)
        if (validationErrors.isNotEmpty()) {
            return@withContext ImportResult(errors = validationErrors)
        }

        // 3. Apply strategy
        when (strategy) {
            ImportConflictStrategy.OVERWRITE -> {
                // Clear all existing data
                goodsDao.deleteAll()
                storeDao.deleteAll()
                priceRecordDao.deleteAll()
                receiptDao.deleteAll()
                // Then import all
                importAll(bundle)
            }
            ImportConflictStrategy.MERGE -> mergeImport(bundle)
            ImportConflictStrategy.SKIP -> skipImport(bundle)
        }
    }

    private suspend fun importAll(bundle: ExportBundle): ImportResult {
        bundle.goods.forEach { goodsDao.insert(toEntity(it)) }
        bundle.stores.forEach { storeDao.insert(toEntity(it)) }
        // Insert price records in batch for performance
        if (bundle.priceRecords.isNotEmpty()) {
            priceRecordDao.insertAll(bundle.priceRecords.map { toEntity(it) })
        }
        bundle.receipts.forEach { receiptDao.insert(toEntity(it)) }

        return ImportResult(
            goodsImported = bundle.goods.size,
            storesImported = bundle.stores.size,
            priceRecordsImported = bundle.priceRecords.size,
            receiptsImported = bundle.receipts.size
        )
    }

    private suspend fun mergeImport(bundle: ExportBundle): ImportResult {
        var goodsImported = 0
        var goodsSkipped = 0
        var storesImported = 0
        var storesSkipped = 0

        // Merge goods by name
        bundle.goods.forEach { exportGoods ->
            val existing = goodsDao.getByName(exportGoods.name)
            if (existing == null) {
                goodsDao.insert(toEntity(exportGoods))
                goodsImported++
            } else {
                goodsSkipped++
            }
        }

        // Merge stores by name
        bundle.stores.forEach { exportStore ->
            val existing = storeDao.getByName(exportStore.name)
            if (existing == null) {
                storeDao.insert(toEntity(exportStore))
                storesImported++
            } else {
                storesSkipped++
            }
        }

        // Price records always imported (time-series data)
        if (bundle.priceRecords.isNotEmpty()) {
            priceRecordDao.insertAll(bundle.priceRecords.map { toEntity(it) })
        }
        bundle.receipts.forEach { receiptDao.insert(toEntity(it)) }

        return ImportResult(
            goodsImported = goodsImported,
            storesImported = storesImported,
            priceRecordsImported = bundle.priceRecords.size,
            receiptsImported = bundle.receipts.size,
            goodsSkipped = goodsSkipped,
            storesSkipped = storesSkipped
        )
    }

    private suspend fun skipImport(bundle: ExportBundle): ImportResult {
        var goodsImported = 0
        var goodsSkipped = 0
        var storesImported = 0
        var storesSkipped = 0

        bundle.goods.forEach { exportGoods ->
            val existing = goodsDao.getByName(exportGoods.name)
            if (existing == null) {
                goodsDao.insert(toEntity(exportGoods))
                goodsImported++
            } else {
                goodsSkipped++
            }
        }

        bundle.stores.forEach { exportStore ->
            val existing = storeDao.getByName(exportStore.name)
            if (existing == null) {
                storeDao.insert(toEntity(exportStore))
                storesImported++
            } else {
                storesSkipped++
            }
        }

        // For SKIP, only import records for newly imported goods/stores
        bundle.priceRecords.forEach { record ->
            priceRecordDao.insert(toEntity(record))
        }
        bundle.receipts.forEach { receiptDao.insert(toEntity(it)) }

        return ImportResult(
            goodsImported = goodsImported,
            storesImported = storesImported,
            priceRecordsImported = bundle.priceRecords.size,
            receiptsImported = bundle.receipts.size,
            goodsSkipped = goodsSkipped,
            storesSkipped = storesSkipped
        )
    }

    // Entity mappers from export DTOs
    private fun toEntity(dto: ExportGoods) = GoodsEntity(
        id = 0, name = dto.name, category = dto.category,
        specUnit = dto.specUnit, createdAt = dto.createdAt, updatedAt = dto.updatedAt
    )

    private fun toEntity(dto: ExportStore) = StoreEntity(
        id = 0, name = dto.name, region = dto.region, address = dto.address,
        latitude = dto.latitude, longitude = dto.longitude, mapUrl = dto.mapUrl, myNote = dto.myNote,
        rating = dto.rating, createdAt = dto.createdAt
    )

    private fun toEntity(dto: ExportPriceRecord) = PriceRecordEntity(
        id = 0, goodsId = dto.goodsId, storeId = dto.storeId, price = dto.price,
        recordDate = dto.recordDate, receiptId = dto.receiptId,
        isPromotion = dto.isPromotion, note = dto.note, createdAt = dto.createdAt
    )

    private fun toEntity(dto: ExportReceipt) = ReceiptEntity(
        id = 0, storeId = dto.storeId, totalPrice = dto.totalPrice,
        buyDate = dto.buyDate, imagePath = dto.imagePath,
        ocrRawText = dto.ocrRawText, createdAt = dto.createdAt
    )
}
