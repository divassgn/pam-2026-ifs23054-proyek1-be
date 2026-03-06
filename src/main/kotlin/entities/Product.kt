package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class Product(
    var id          : String = UUID.randomUUID().toString(),
    var userId      : String,
    var name        : String,
    var description : String,
    var category    : String,
    var unit        : String,
    @Contextual
    var price       : BigDecimal,
    var stock       : Int = 0,
    var minStock    : Int = 0,
    var image       : String? = null,

    @Contextual
    val createdAt   : Instant = Clock.System.now(),
    @Contextual
    var updatedAt   : Instant = Clock.System.now(),
)
