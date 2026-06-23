package com.pricekeeper.app.feature.goods

import com.pricekeeper.app.domain.model.GoodsPriceDetail

/**
 * Sealed interface for GoodsDetail screen state.
 */
sealed interface GoodsDetailUiState {
    data object Loading : GoodsDetailUiState
    data class Success(val data: GoodsPriceDetail) : GoodsDetailUiState
    data class Error(val message: String) : GoodsDetailUiState
}
