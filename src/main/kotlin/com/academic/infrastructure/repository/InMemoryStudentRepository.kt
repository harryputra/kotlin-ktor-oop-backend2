package org.example.com.academic.infrastructure.repository

import org.example.com.academic.domain.Student
import org.example.com.academic.domain.StudentId
import org.example.com.academic.interfaces.repository.StudentRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryStudentRepository : StudentRepository {
    private val byId = ConcurrentHashMap<String, Student>()
    private val byEmail = ConcurrentHashMap<String, Student>()

    override suspend fun save(student: Student): Student {
        byId[student.id.value] = student
        byEmail[student.email.lowercase()] = student
        return student
    }
    override suspend fun findById(id: StudentId): Student? = byId[id.value]
    override suspend fun findByEmail(email: String): Student? = byEmail[email.lowercase()]
    override suspend fun findAll(): List<Student> = byId.values.toList()
}