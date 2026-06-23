package com.pricekeeper.app.feature.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.repository.StoreRepository
import com.pricekeeper.app.domain.usecase.GetStoreDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStoreDetailUseCase: GetStoreDetailUseCase,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val storeId: Long = savedStateHandle.get<Long>("storeId") ?: -1L

    private val _uiState =
        MutableStateFlow<StoreDetailUiState>(StoreDetailUiState.Loading)
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = StoreDetailUiState.Loading
            try {
                val detail = getStoreDetailUseCase(storeId)
                if (detail != null) {
                    _uiState.value = StoreDetailUiState.Success(
                        store = detail.store,
                        goodsSummaries = detail.goodsSummaries,
                        reviewDraft = detail.store.myNote.orEmpty()
                    )
                } else {
                    _uiState.value = StoreDetailUiState.Error("商店不存在")
                }
            } catch (e: Exception) {
                _uiState.value = StoreDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun onReviewChange(review: String) {
        val state = _uiState.value
        if (state is StoreDetailUiState.Success) {
            _uiState.value = state.copy(reviewDraft = review)
        }
    }

    fun saveReview() {
        val state = _uiState.value
        if (state is StoreDetailUiState.Success) {
            val updated = state.store.copy(myNote = state.reviewDraft.trim().ifBlank { null })
            viewModelScope.launch {
                _uiState.value = state.copy(isSavingReview = true)
                storeRepository.updateStore(updated)
                _uiState.value = state.copy(store = updated, isSavingReview = false)
            }
        }
    }
}
