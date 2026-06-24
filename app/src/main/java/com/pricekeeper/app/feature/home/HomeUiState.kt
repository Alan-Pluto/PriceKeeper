package com.pricekeeper.app.feature.home

import com.pricekeeper.app.domain.model.RecentPriceRecord

/**
 * UI state for the Home (记一笔) screen.
 */
data class HomeUiState(
    val goodsCount: Int = 0,
    val storeCount: Int = 0,
    val recentRecords: List<RecentPriceRecord> = emptyList(),
    val isLoading: Boolean = true
)
