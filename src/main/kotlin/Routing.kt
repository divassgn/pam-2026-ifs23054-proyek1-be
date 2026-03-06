package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.ProductService
import org.delcom.services.AuthService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val productService : ProductService by inject()
    val authService    : AuthService    by inject()
    val userService    : UserService    by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                status  = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status  = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data    = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status  = HttpStatusCode.fromValue(500),
                message = ErrorResponse(status = "error", message = cause.message ?: "Unknown error", data = "")
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Manajemen Inventaris berjalan. Dibuat oleh Sri Diva Siagian.")
        }

        // Auth routes
        route("/auth") {
            post("/login")         { authService.postLogin(call) }
            post("/register")      { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout")        { authService.postLogout(call) }
        }

        authenticate(JWTConstants.NAME) {
            // User routes
            route("/users") {
                get("/me")           { userService.getMe(call) }
                put("/me")           { userService.putMe(call) }
                put("/me/password")  { userService.putMyPassword(call) }
                put("/me/photo")     { userService.putMyPhoto(call) }
            }

            // Product / Inventaris routes
            route("/products") {
                get("/stats")     { productService.getStats(call) }   // statistik home
                get              { productService.getAll(call) }       // list + search + filter + pagination
                post             { productService.post(call) }         // tambah produk
                get("/{id}")     { productService.getById(call) }      // detail produk
                put("/{id}")     { productService.put(call) }          // update produk
                put("/{id}/image") { productService.putImage(call) }   // update gambar
                delete("/{id}") { productService.delete(call) }        // hapus produk
            }
        }

        // Static image serving
        route("/images") {
            get("users/{id}")    { userService.getPhoto(call) }
            get("products/{id}") { productService.getImage(call) }
        }
    }
}
