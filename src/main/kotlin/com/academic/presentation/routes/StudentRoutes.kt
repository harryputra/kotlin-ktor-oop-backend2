package org.example.com.academic.presentation.routes

import org.example.com.academic.application.StudentService
import org.example.com.academic.domain.StudentId
import org.example.com.academic.presentation.dto.CreateStudentRequest
import org.example.com.academic.presentation.dto.StudentResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureStudentRoutes() {
    val studentService by inject<StudentService>()

    route("/students") {
        post {
            val req = call.receive<CreateStudentRequest>()
            val created = studentService.createStudent(req.name, req.email)
            call.respond(HttpStatusCode.Created, StudentResponse(created.id.value, created.name, created.email))
        }
        get {
            val list = studentService.listStudents().map {
                StudentResponse(it.id.value, it.name, it.email)
            }
            call.respond(list)
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val found = studentService.getStudentById(StudentId(id))
            if (found == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(StudentResponse(found.id.value, found.name, found.email))
        }
    }
}