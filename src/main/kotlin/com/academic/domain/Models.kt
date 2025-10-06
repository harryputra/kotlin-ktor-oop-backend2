package org.example.com.academic.domain

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: StudentId,
    val name: String,
    val email: String
) {
    init {
        require(name.isNotBlank()) { "Student name cannot be blank." }
        require(isValidEmail(email)) { "Invalid email format for student." }
    }
}

@Serializable
data class Course(
    val id: CourseId,
    val code: String,
    val name: String,
    val credits: Int
) {
    init {
        require(code.isNotBlank() && code.length <= 10) { "Course code cannot be blank and must be at most 10 characters." }
        require(name.isNotBlank()) { "Course name cannot be blank." }
        require(credits in 1..10) { "Course credits must be between 1 and 10." }
    }
}

@Serializable
data class Enrollment(
    val id: EnrollmentId,
    val studentId: StudentId,
    val courseId: CourseId
)