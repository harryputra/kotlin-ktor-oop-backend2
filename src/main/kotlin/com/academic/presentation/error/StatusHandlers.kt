package org.example.com.academic.presentation.error

import org.example.com.academic.domain.AlreadyExistsException
import org.example.com.academic.domain.BusinessRuleException
import org.example.com.academic.domain.NotFoundException
import org.example.com.academic.domain.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.configureStatusPages() {
    exception<ValidationException> { call, e ->
        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Validation error")))
    }
    exception<NotFoundException> { call, e ->
        call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Not found")))
    }
    exception<AlreadyExistsException> { call, e ->
        call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Conflict")))
    }
    exception<BusinessRuleException> { call, e ->
        call.respond(HttpStatusCode.UnprocessableEntity, mapOf("error" to (e.message ?: "Business rule violated")))
    }
    exception<Throwable> { call, e ->
        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal error")))
    }
}