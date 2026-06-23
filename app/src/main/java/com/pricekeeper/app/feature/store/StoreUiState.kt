package com.pricekeeper.app.feature.store

import com.pricekeeper.app.domain.model.Store

/**
 * UI state for the Store (商店) screen.
 * Stores grouped by region for the region-based LazyColumn display.
 */
data class StoreUiState(
    val storesByRegion: Map<String, List<Store>> = emptyMap(),
    val allStores: List<Store> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    /** Filtered stores matching the search query. */
    val filteredStores: List<Store>
        get() = if (searchQuery.isBlank()) {
            allStores
        } else {
            allStores.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    /** Filtered stores grouped by region. */
    val filteredStoresByRegion: Map<String, List<Store>>
        get() = filteredStores.groupBy { it.region }
}
