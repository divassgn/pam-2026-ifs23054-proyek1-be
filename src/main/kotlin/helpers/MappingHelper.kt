package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.ProductDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Product
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.createdAt,
    dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun productDAOToModel(dao: ProductDAO) = Product(
    id          = dao.id.value.toString(),
    userId      = dao.userId.toString(),
    name        = dao.name,
    description = dao.description,
    category    = dao.category,
    unit        = dao.unit,
    price       = dao.price.toDouble(),   // BigDecimal (dari DB) → Double
    stock       = dao.stock,
    minStock    = dao.minStock,
    image       = dao.image,
    createdAt   = dao.createdAt,
    updatedAt   = dao.updatedAt,
)