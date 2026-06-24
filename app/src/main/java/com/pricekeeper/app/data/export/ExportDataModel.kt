package com.pricekeeper.app.data.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportBundle(
    val version: String = CURRENT_VERSION,
    val exportedAt: Long,
    val goods: List<ExportGoods>,
    val stores: List<ExportStore>,
    val priceRecords: List<ExportPriceRecord>
) {
    companion object {
        const val CURRENT_VERSION = "1.0"
    }
}

@Serializable
data class ExportGoods(
    val id: Long,
    val name: String,
    val category: String,
    val specUnit: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ExportStore(
    val id: Long,
    val name: String,
    val region: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val mapUrl: String? = null,
    val myNote: String?,
    val rating: Int,
    val createdAt: Long
)

@Serializable
data class ExportPriceRecord(
    val id: Long,
    val goodsId: Long,
    val storeId: Long,
    val price: Double,
    val recordDate: Long,
    val isPromotion: Boolean,
    val note: String?,
    val createdAt: Long
)
