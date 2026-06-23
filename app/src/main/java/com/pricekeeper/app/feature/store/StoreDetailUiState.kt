package com.pricekeeper.app.feature.store

import com.pricekeeper.app.domain.model.Store
import com.pricekeeper.app.domain.model.StoreGoodsItem

sealed interface StoreDetailUiState {
    data object Loading : StoreDetailUiState
    data class Success(
        val store: Store,
        val goodsSummaries: List<StoreGoodsItem>,
        val reviewDraft: String = store.myNote.orEmpty(),
        val isSavingReview: Boolean = false
    ) : StoreDetailUiState
    data class Error(val message: String) : StoreDetailUiState
}
