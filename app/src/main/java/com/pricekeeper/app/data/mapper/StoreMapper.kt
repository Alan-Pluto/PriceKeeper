package com.pricekeeper.app.data.mapper

import com.pricekeeper.app.data.local.entity.StoreEntity
import com.pricekeeper.app.domain.model.Store

fun StoreEntity.toDomain(): Store = Store(
    id = id,
    name = name,
    region = region,
    address = address,
    latitude = latitude,
    longitude = longitude,
    mapUrl = mapUrl,
    myNote = myNote,
    rating = rating,
    createdAt = createdAt
)

fun Store.toEntity(): StoreEntity = StoreEntity(
    id = id,
    name = name,
    region = region,
    address = address,
    latitude = latitude,
    longitude = longitude,
    mapUrl = mapUrl,
    myNote = myNote,
    rating = rating,
    createdAt = createdAt
)
