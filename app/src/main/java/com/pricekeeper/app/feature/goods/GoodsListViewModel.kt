package com.pricekeeper.app.feature.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.model.Goods
import com.pricekeeper.app.domain.repository.GoodsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoodsListUiState(
    val goodsByCategory: Map<String, List<Goods>> = emptyMap(),
    val minPrices: Map<Long, Double> = emptyMap(),
    val categoryCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoodsListViewModel @Inject constructor(
    goodsRepository: GoodsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoodsListUiState())
    val uiState: StateFlow<GoodsListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                goodsRepository.observeGoods(),
                goodsRepository.observeGoodsMinPrices()
            ) { goods, minPrices ->
                val byCategory = goods.groupBy { it.category }
                val counts = byCategory.mapValues { it.value.size }
                GoodsListUiState(
                    goodsByCategory = byCategory,
                    categoryCounts = counts,
                    minPrices = minPrices,
                    isLoading = false
                )
            }.collect { nextState ->
                _uiState.update { nextState }
            }
        }
    }
}
