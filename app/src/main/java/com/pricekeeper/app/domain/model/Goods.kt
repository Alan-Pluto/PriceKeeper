package com.pricekeeper.app.domain.model

/**
 * Domain model for a goods item.
 * Decoupled from Room entity — mappers handle the conversion.
 */
data class Goods(
    val id: Long,
    val name: String,
    val category: String,
    val specUnit: String?,
    val createdAt: Long,
    val updatedAt: Long
)
