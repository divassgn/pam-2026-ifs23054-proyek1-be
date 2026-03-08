package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.ProductStatsResponse
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.ProductRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IProductRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class ProductService(
    private val userRepo    : IUserRepository,
    private val productRepo : IProductRepository
) {

    // GET /products?search=&page=&perPage=&category=&lowStock=true
    suspend fun getAll(call: ApplicationCall) {
        val user     = ServiceHelper.getAuthUser(call, userRepo)
        val search   = call.request.queryParameters["search"] ?: ""
        val page     = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage  = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10
        val category = call.request.queryParameters["category"]
        val lowStock = call.request.queryParameters["lowStock"]?.toBooleanStrictOrNull()
        val products = productRepo.getAll(user.id, search, page, perPage, category, lowStock)
        call.respond(DataResponse("success", "Berhasil mengambil daftar produk", mapOf("products" to products)))
    }

    // GET /products/stats
    suspend fun getStats(call: ApplicationCall) {
        val user  = ServiceHelper.getAuthUser(call, userRepo)
        val stats = productRepo.getHomeStats(user.id)
        call.respond(DataResponse<ProductStatsResponse>(
            status  = "success",
            message = "Berhasil mengambil statistik inventaris",
            data    = ProductStatsResponse(stats)
        ))
    }

    // GET /products/{id}
    suspend fun getById(call: ApplicationCall) {
        val productId = call.parameters["id"] ?: throw AppException(400, "ID produk tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)
        val product   = productRepo.getById(productId)
        if (product == null || product.userId != user.id)
            throw AppException(404, "Produk tidak ditemukan!")
        call.respond(DataResponse("success", "Berhasil mengambil data produk", mapOf("product" to product)))
    }

    // POST /products
    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<ProductRequest>()
        request.userId = user.id
        val validator = ValidatorHelper(request.toMap())
        validator.required("name",        "Nama produk tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("category",    "Kategori tidak boleh kosong")
        validator.required("unit",        "Satuan tidak boleh kosong")
        validator.validate()
        val productId = productRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan produk", mapOf("productId" to productId)))
    }

    // PUT /products/{id}
    suspend fun put(call: ApplicationCall) {
        val productId = call.parameters["id"] ?: throw AppException(400, "ID produk tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)
        val request   = call.receive<ProductRequest>()
        request.userId = user.id
        val validator = ValidatorHelper(request.toMap())
        validator.required("name",        "Nama produk tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("category",    "Kategori tidak boleh kosong")
        validator.required("unit",        "Satuan tidak boleh kosong")
        validator.validate()
        val old = productRepo.getById(productId)
        if (old == null || old.userId != user.id)
            throw AppException(404, "Produk tidak ditemukan!")
        request.image = old.image
        val updated = productRepo.update(user.id, productId, request.toEntity())
        if (!updated) throw AppException(400, "Gagal memperbarui produk!")
        call.respond(DataResponse("success", "Berhasil memperbarui produk", null))
    }

    // PUT /products/{id}/image
    suspend fun putImage(call: ApplicationCall) {
        val productId = call.parameters["id"] ?: throw AppException(400, "ID produk tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)
        val request   = ProductRequest()
        request.userId = user.id
        val multipart = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext      = part.originalFileName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/products/$fileName"
                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        request.image = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }
        if (request.image == null) throw AppException(400, "Gambar produk tidak tersedia!")
        val old = productRepo.getById(productId)
        if (old == null || old.userId != user.id) throw AppException(404, "Produk tidak ditemukan!")
        request.apply {
            name = old.name; description = old.description; category = old.category
            unit = old.unit; price = old.price; stock = old.stock; minStock = old.minStock
        }
        val updated = productRepo.update(user.id, productId, request.toEntity())
        if (!updated) throw AppException(400, "Gagal memperbarui gambar produk!")
        old.image?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil mengubah gambar produk", null))
    }

    // DELETE /products/{id}
    suspend fun delete(call: ApplicationCall) {
        val productId = call.parameters["id"] ?: throw AppException(400, "ID produk tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)
        val old       = productRepo.getById(productId)
        if (old == null || old.userId != user.id) throw AppException(404, "Produk tidak ditemukan!")
        val deleted = productRepo.delete(user.id, productId)
        if (!deleted) throw AppException(400, "Gagal menghapus produk!")
        old.image?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil menghapus produk", null))
    }

    // GET /images/products/{id}
    suspend fun getImage(call: ApplicationCall) {
        val productId = call.parameters["id"] ?: throw AppException(400, "ID produk tidak valid!")
        val product   = productRepo.getById(productId) ?: return call.respond(HttpStatusCode.NotFound)
        if (product.image == null) throw AppException(404, "Produk belum memiliki gambar")
        val file = File(product.image!!)
        if (!file.exists()) throw AppException(404, "Gambar produk tidak tersedia")
        call.respondFile(file)
    }
}