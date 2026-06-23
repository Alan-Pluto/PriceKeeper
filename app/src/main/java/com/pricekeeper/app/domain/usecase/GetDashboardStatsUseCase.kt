package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.DashboardStats
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.PriceRecordRepository
import com.pricekeeper.app.domain.repository.ReceiptRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val storeRepository: StoreRepository,
    private val priceRecordRepository: PriceRecordRepository,
    private val receiptRepository: ReceiptRepository
) {
    operator fun invoke(): Flow<DashboardStats> = combine(
        goodsRepository.observeGoods(),
        storeRepository.observeStores(),
        receiptRepository.observeReceipts()
    ) { goods, stores, receipts ->
        DashboardStats(
            goodsCount = goods.size,
            storeCount = stores.size,
            totalSpending = 0.0, // Will be populated async
            receiptCount = receipts.size,
            priceRecordCount = 0
        )
    }
}
