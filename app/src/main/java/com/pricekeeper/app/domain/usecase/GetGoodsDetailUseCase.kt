package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.model.GoodsPriceDetail
import com.pricekeeper.app.domain.repository.GoodsRepository
import javax.inject.Inject

/**
 * Returns aggregated price detail for a goods item, including
 * min/max/latest prices, price trend, and store comparison.
 */
class GetGoodsDetailUseCase @Inject constructor(
    private val goodsRepository: GoodsRepository
) {
    suspend operator fun invoke(goodsId: Long): GoodsPriceDetail? {
        return goodsRepository.getGoodsDetail(goodsId)
    }
}
