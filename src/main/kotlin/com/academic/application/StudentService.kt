package org.example.com.academic.application

import org.example.com.academic.domain.Student
import org.example.com.academic.domain.StudentId
import org.example.com.academic.domain.ValidationException
import org.example.com.academic.interfaces.repository.StudentRepository

class StudentService(private val studentRepository: StudentRepository) {
    suspend fun createStudent(name: String, email: String): Student {
        if (studentRepository.findByEmail(email) != null) {
            throw ValidationException("Student with email '$email' already exists.")
        }
        val student = Student(id = StudentId(), name = name, email = email)
        studentRepository.save(student)
        return student
    }

    suspend fun getStudentById(id: StudentId): Student? =
        studentRepository.findById(id)

    suspend fun listStudents(): List<Student> =
        studentRepository.findAll()
}