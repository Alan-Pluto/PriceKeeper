package com.pricekeeper.app.data.mapper

import com.pricekeeper.app.data.local.entity.ReceiptEntity
import com.pricekeeper.app.domain.model.Receipt

fun ReceiptEntity.toDomain(): Receipt = Receipt(
    id = id,
    storeId = storeId,
    totalPrice = totalPrice,
    buyDate = buyDate,
    imagePath = imagePath,
    ocrRawText = ocrRawText,
    createdAt = createdAt
)

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    storeId = storeId,
    totalPrice = totalPrice,
    buyDate = buyDate,
    imagePath = imagePath,
    ocrRawText = ocrRawText,
    createdAt = createdAt
)
