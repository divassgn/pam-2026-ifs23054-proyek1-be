package org.delcom.repositories

import org.delcom.entities.Product

interface IProductRepository {
    suspend fun getAll(
        userId   : String,
        search   : String,
        page     : Int,
        perPage  : Int,
        category : String?,
        lowStock : Boolean?
    ): List<Product>

    suspend fun getHomeStats(userId: String): Map<String, Any>
    suspend fun getById(productId: String): Product?
    suspend fun create(product: Product): String
    suspend fun update(userId: String, productId: String, newProduct: Product): Boolean
    suspend fun delete(userId: String, productId: String): Boolean
}
