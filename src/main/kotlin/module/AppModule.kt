package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.ProductService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {

    single<IUserRepository> { UserRepository() }
    single { UserService(get(), get()) }

    single<IRefreshTokenRepository> { RefreshTokenRepository() }
    single { AuthService(jwtSecret, get(), get()) }

    single<IProductRepository> { ProductRepository() }
    single { ProductService(get(), get()) }
}
