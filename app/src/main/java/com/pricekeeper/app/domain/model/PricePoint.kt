package com.pricekeeper.app.domain.model

/**
 * A single data point on the price trend chart.
 */
data class PricePoint(
    val timestamp: Long,
    val price: Double
)
