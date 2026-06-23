package com.pricekeeper.app.data.mapper

import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.domain.model.Goods

fun GoodsEntity.toDomain(): Goods = Goods(
    id = id,
    name = name,
    category = category,
    specUnit = specUnit,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Goods.toEntity(): GoodsEntity = GoodsEntity(
    id = id,
    name = name,
    category = category,
    specUnit = specUnit,
    createdAt = createdAt,
    updatedAt = updatedAt
)
