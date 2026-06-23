package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.StoreWithGoods
import com.pricekeeper.app.domain.repository.StoreRepository
import javax.inject.Inject

/**
 * Returns a store with all its tracked goods and price summaries.
 */
class GetStoreDetailUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(storeId: Long): StoreWithGoods? {
        return storeRepository.getStoreDetail(storeId)
    }
}
