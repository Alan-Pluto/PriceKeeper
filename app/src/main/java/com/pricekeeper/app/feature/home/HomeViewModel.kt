package com.pricekeeper.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.PriceRecordRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    goodsRepository: GoodsRepository,
    storeRepository: StoreRepository,
    priceRecordRepository: PriceRecordRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        goodsRepository.observeGoods(),
        storeRepository.observeStores(),
        priceRecordRepository.observeRecentPriceRecords(limit = 5)
    ) { goods, stores, recentRecords ->
        HomeUiState(
            goodsCount = goods.size,
            storeCount = stores.size,
            recentRecords = recentRecords,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
