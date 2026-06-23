package com.pricekeeper.app.domain.model

data class DashboardStats(
    val goodsCount: Int = 0,
    val storeCount: Int = 0,
    val totalSpending: Double = 0.0,
    val receiptCount: Int = 0,
    val priceRecordCount: Int = 0
)
