package com.pricekeeper.app.feature.home

/**
 * UI state for the Home (记一笔) screen.
 */
data class HomeUiState(
    val goodsCount: Int = 0,
    val storeCount: Int = 0,
    val lastRecordDate: Long? = null,
    val isLoading: Boolean = true
)
