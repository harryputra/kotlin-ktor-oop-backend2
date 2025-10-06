package org.example.com.academic.di

import org.example.com.academic.application.StudentService
import org.example.com.academic.infrastructure.repository.InMemoryStudentRepository
import org.example.com.academic.interfaces.repository.StudentRepository
import org.koin.dsl.module

val appModule = module {
    single<StudentRepository> { InMemoryStudentRepository() }
    single { StudentService(get()) }
}