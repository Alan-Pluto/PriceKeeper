package com.pricekeeper.app.feature.manual

import com.pricekeeper.app.domain.model.Store

data class ManualEntryUiState(
    val goodsName: String = "",
    val price: String = "",
    val category: String = "",
    val storeName: String = "",
    val storeRegion: String = "",
    val storeAddress: String = "",
    val storeLatitude: Double? = null,
    val storeLongitude: Double? = null,
    val storeMapUrl: String? = null,
    val storeLocationInput: String = "",
    val categories: List<String> = emptyList(),
    val existingStores: List<Store> = emptyList(),
    val showCategorySheet: Boolean = false,
    val showStoreSheet: Boolean = false,
    val categorySearchQuery: String = "",
    val storeSearchQuery: String = "",
    val showNewStoreFields: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val pendingBackAfterSave: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
) {
    val goodsNameError: String? get() = validationErrors["goodsName"]
    val priceError: String? get() = validationErrors["price"]
    val storeNameError: String? get() = validationErrors["storeName"]
    val storeLocationError: String? get() = validationErrors["storeLocation"]

    val filteredCategories: List<String>
        get() = if (categorySearchQuery.isBlank()) {
            categories
        } else {
            categories.filter { it.contains(categorySearchQuery.trim(), ignoreCase = true) }
        }

    val filteredStores: List<Store>
        get() = if (storeSearchQuery.isBlank()) {
            existingStores
        } else {
            existingStores.filter {
                it.name.contains(storeSearchQuery.trim(), ignoreCase = true) ||
                    it.region.contains(storeSearchQuery.trim(), ignoreCase = true) ||
                    it.address.orEmpty().contains(storeSearchQuery.trim(), ignoreCase = true)
            }
        }
}
