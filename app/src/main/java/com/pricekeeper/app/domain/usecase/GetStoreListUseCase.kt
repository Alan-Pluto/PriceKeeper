package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.Store
import com.pricekeeper.app.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a Flow of all stores, ordered by name.
 */
class GetStoreListUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    operator fun invoke(): Flow<List<Store>> = storeRepository.observeStores()
}
