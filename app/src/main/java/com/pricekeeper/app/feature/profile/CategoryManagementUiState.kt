package com.pricekeeper.app.feature.profile

data class CategoryManagementUiState(
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val newCategoryName: String = "",
    val editingCategory: String? = null,
    val editName: String = ""
)
