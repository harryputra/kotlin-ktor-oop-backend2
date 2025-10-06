package org.example.com.academic.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline @Serializable value class StudentId(val value: String = UUID.randomUUID().toString())
@JvmInline @Serializable value class CourseId(val value: String = UUID.randomUUID().toString())
@JvmInline @Serializable value class EnrollmentId(val value: String = UUID.randomUUID().toString())