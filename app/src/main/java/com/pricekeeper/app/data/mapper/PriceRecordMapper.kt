package com.pricekeeper.app.data.mapper

import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import com.pricekeeper.app.domain.model.PriceRecord

fun PriceRecordEntity.toDomain(): PriceRecord = PriceRecord(
    id = id,
    goodsId = goodsId,
    storeId = storeId,
    price = price,
    recordDate = recordDate,
    isPromotion = isPromotion,
    note = note,
    createdAt = createdAt
)

fun PriceRecord.toEntity(): PriceRecordEntity = PriceRecordEntity(
    id = id,
    goodsId = goodsId,
    storeId = storeId,
    price = price,
    recordDate = recordDate,
    isPromotion = isPromotion,
    note = note,
    createdAt = createdAt
)
