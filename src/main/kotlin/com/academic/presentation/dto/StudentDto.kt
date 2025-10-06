package org.example.com.academic.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudentRequest(val name: String, val email: String)

@Serializable
data class StudentResponse(val id: String, val name: String, val email: String)