package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class ProductStatsData(
    val total: Long = 0,
    val lowStock: Long = 0,
    val outOfStock: Long = 0,
    val totalValue: Double = 0.0
)

@Serializable
data class ProductStatsResponse(
    val stats: ProductStatsData
)