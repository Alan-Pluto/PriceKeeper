package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.DashboardStats
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val storeRepository: StoreRepository
) {
    operator fun invoke(): Flow<DashboardStats> = combine(
        goodsRepository.observeGoods(),
        storeRepository.observeStores()
    ) { goods, stores ->
        DashboardStats(
            goodsCount = goods.size,
            storeCount = stores.size
        )
    }
}
