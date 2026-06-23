package com.pricekeeper.app.domain.model

/**
 * Domain model for a store.
 */
data class Store(
    val id: Long,
    val name: String,
    val region: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val mapUrl: String?,
    val myNote: String?,
    val rating: Int,
    val createdAt: Long
)
