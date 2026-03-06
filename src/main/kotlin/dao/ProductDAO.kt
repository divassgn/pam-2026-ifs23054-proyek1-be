package org.delcom.dao

import org.delcom.tables.ProductTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ProductDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, ProductDAO>(ProductTable)

    var userId      by ProductTable.userId
    var name        by ProductTable.name
    var description by ProductTable.description
    var category    by ProductTable.category
    var unit        by ProductTable.unit
    var price       by ProductTable.price
    var stock       by ProductTable.stock
    var minStock    by ProductTable.minStock
    var image       by ProductTable.image
    var createdAt   by ProductTable.createdAt
    var updatedAt   by ProductTable.updatedAt
}
