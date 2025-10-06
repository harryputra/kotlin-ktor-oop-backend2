package org.example.com.academic.presentation.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureHealthRoutes() {
    route("/health") {
        get {
            call.respond(mapOf("status" to "UP"))
        }
    }
}