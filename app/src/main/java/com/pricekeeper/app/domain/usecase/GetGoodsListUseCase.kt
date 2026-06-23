package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.Goods
import com.pricekeeper.app.domain.repository.GoodsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a Flow of all goods, ordered by most recently updated.
 */
class GetGoodsListUseCase @Inject constructor(
    private val goodsRepository: GoodsRepository
) {
    operator fun invoke(): Flow<List<Goods>> = goodsRepository.observeGoods()
}
