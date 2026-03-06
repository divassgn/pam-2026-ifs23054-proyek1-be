package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ProductTable : UUIDTable("products") {
    val userId      = uuid("user_id")
    val name        = varchar("name", 150)
    val description = text("description")
    val category    = varchar("category", 100)
    val unit        = varchar("unit", 50)            // satuan: pcs, kg, liter, box, dll
    val price       = decimal("price", 15, 2)        // harga per satuan
    val stock       = integer("stock").default(0)     // jumlah stok saat ini
    val minStock    = integer("min_stock").default(0) // batas stok minimum (untuk alert)
    val image       = text("image").nullable()
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
