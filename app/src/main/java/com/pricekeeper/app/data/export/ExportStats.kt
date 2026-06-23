package com.pricekeeper.app.data.export

data class ExportStats(
    val goodsCount: Int,
    val storeCount: Int,
    val priceRecordCount: Int,
    val receiptCount: Int,
    val fileSizeBytes: Long
)
