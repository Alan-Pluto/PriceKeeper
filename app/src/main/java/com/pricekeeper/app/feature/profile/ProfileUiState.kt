package com.pricekeeper.app.feature.profile

import com.pricekeeper.app.domain.model.DashboardStats
import com.pricekeeper.app.data.export.ExportStats
import com.pricekeeper.app.data.export.ImportResult

data class ProfileUiState(
    val dashboardStats: DashboardStats = DashboardStats(),
    val isExporting: Boolean = false,
    val exportResult: ExportStats? = null,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null,
    val isDarkMode: Boolean = false
)
