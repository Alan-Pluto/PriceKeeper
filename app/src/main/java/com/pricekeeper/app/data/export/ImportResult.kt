package com.pricekeeper.app.data.export

data class ImportResult(
    val goodsImported: Int = 0,
    val storesImported: Int = 0,
    val priceRecordsImported: Int = 0,
    val goodsSkipped: Int = 0,
    val storesSkipped: Int = 0,
    val errors: List<String> = emptyList()
) {
    val isSuccess: Boolean get() = errors.isEmpty()
    val totalImported: Int get() = goodsImported + storesImported + priceRecordsImported
}
