package org.example.com.academic.interfaces.repository

import org.example.com.academic.domain.Student
import org.example.com.academic.domain.StudentId

interface StudentRepository {
    suspend fun save(student: Student): Student
    suspend fun findById(id: StudentId): Student?
    suspend fun findByEmail(email: String): Student?
    suspend fun findAll(): List<Student>
}