package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Product

@Serializable
data class ProductRequest(
    var userId      : String = "",
    var name        : String = "",
    var description : String = "",
    var category    : String = "",
    var unit        : String = "",
    var price       : Double = 0.0,
    var stock       : Int = 0,
    var minStock    : Int = 0,
    var image       : String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"      to userId,
        "name"        to name,
        "description" to description,
        "category"    to category,
        "unit"        to unit,
        "price"       to price,
        "stock"       to stock,
        "minStock"    to minStock,
        "image"       to image,
    )

    fun toEntity(): Product = Product(
        userId      = userId,
        name        = name,
        description = description,
        category    = category,
        unit        = unit,
        price       = price,        // langsung Double, tidak perlu BigDecimal.valueOf()
        stock       = stock,
        minStock    = minStock,
        image       = image,
        updatedAt   = Clock.System.now()
    )
}