package com.pricekeeper.app.feature.goods

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.usecase.GetGoodsDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoodsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGoodsDetailUseCase: GetGoodsDetailUseCase
) : ViewModel() {

    private val goodsId: Long = savedStateHandle.get<Long>("goodsId") ?: -1L

    private val _uiState = MutableStateFlow<GoodsDetailUiState>(GoodsDetailUiState.Loading)
    val uiState: StateFlow<GoodsDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = GoodsDetailUiState.Loading
            try {
                val detail = getGoodsDetailUseCase(goodsId)
                if (detail != null) {
                    _uiState.value = GoodsDetailUiState.Success(detail)
                } else {
                    _uiState.value = GoodsDetailUiState.Error("商品不存在")
                }
            } catch (e: Exception) {
                _uiState.value = GoodsDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}
