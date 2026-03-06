package org.delcom.repositories

import org.delcom.dao.ProductDAO
import org.delcom.entities.Product
import org.delcom.helpers.productDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ProductTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.andWhere
import java.math.BigDecimal
import java.util.UUID

class ProductRepository : IProductRepository {

    override suspend fun getAll(
        userId   : String,
        search   : String,
        page     : Int,
        perPage  : Int,
        category : String?,
        lowStock : Boolean?
    ): List<Product> = suspendTransaction {
        var query = ProductDAO.find {
            ProductTable.userId eq UUID.fromString(userId)
        }

        // Filter search by name
        if (search.isNotBlank()) {
            query = ProductDAO.find {
                (ProductTable.userId eq UUID.fromString(userId)) and
                (ProductTable.name.lowerCase() like "%${search.lowercase()}%")
            }
        }

        val allResults = query
            .orderBy(ProductTable.createdAt to SortOrder.DESC)
            .map(::productDAOToModel)

        // Filter category
        val filtered = if (!category.isNullOrBlank()) {
            allResults.filter { it.category.equals(category, ignoreCase = true) }
        } else allResults

        // Filter low stock (stok <= minStock)
        val filteredStock = if (lowStock == true) {
            filtered.filter { it.stock <= it.minStock }
        } else filtered

        // Pagination
        val offset = (page - 1) * perPage
        filteredStock.drop(offset).take(perPage)
    }

    override suspend fun getHomeStats(userId: String): Map<String, Any> = suspendTransaction {
        val products = ProductDAO.find { ProductTable.userId eq UUID.fromString(userId) }
            .map(::productDAOToModel)

        val total      = products.size.toLong()
        val lowStock   = products.count { it.stock <= it.minStock }.toLong()
        val outOfStock = products.count { it.stock == 0 }.toLong()
        val totalValue = products.fold(BigDecimal.ZERO) { acc, p ->
            acc + p.price.multiply(BigDecimal(p.stock))
        }

        mapOf(
            "total"      to total,
            "lowStock"   to lowStock,
            "outOfStock" to outOfStock,
            "totalValue" to totalValue.toDouble()
        )
    }

    override suspend fun getById(productId: String): Product? = suspendTransaction {
        ProductDAO
            .find { ProductTable.id eq UUID.fromString(productId) }
            .limit(1)
            .map(::productDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(product: Product): String = suspendTransaction {
        val dao = ProductDAO.new {
            userId      = UUID.fromString(product.userId)
            name        = product.name
            description = product.description
            category    = product.category
            unit        = product.unit
            price       = product.price
            stock       = product.stock
            minStock    = product.minStock
            image       = product.image
            createdAt   = product.createdAt
            updatedAt   = product.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, productId: String, newProduct: Product): Boolean = suspendTransaction {
        val dao = ProductDAO
            .find {
                (ProductTable.id eq UUID.fromString(productId)) and
                (ProductTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.name        = newProduct.name
            dao.description = newProduct.description
            dao.category    = newProduct.category
            dao.unit        = newProduct.unit
            dao.price       = newProduct.price
            dao.stock       = newProduct.stock
            dao.minStock    = newProduct.minStock
            dao.image       = newProduct.image
            dao.updatedAt   = newProduct.updatedAt
            true
        } else false
    }

    override suspend fun delete(userId: String, productId: String): Boolean = suspendTransaction {
        val rows = ProductTable.deleteWhere {
            (ProductTable.id eq UUID.fromString(productId)) and
            (ProductTable.userId eq UUID.fromString(userId))
        }
        rows >= 1
    }
}
