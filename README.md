# Kotlin Ktor OOP Backend — Mini Academic System

Backend mini berbasis **Kotlin + Ktor + Koin** dengan arsitektur berlapis (Presentation → Application → Domain → Infrastructure). Cocok sebagai **modul praktikum** untuk mahasiswa yang baru belajar backend modern di JVM.

> Status: **Tahap 1** (entity Student, in-memory repository, endpoint dasar).  
> Tahap selanjutnya: Course & Enrollment, idempoten, dan integrasi database (PostgreSQL).

---

## ✨ Fitur Utama
- **Ktor Server (Netty)** dengan JSON (kotlinx.serialization) & **StatusPages** untuk error terkelola.
- **Dependency Injection** via **Koin** (loose coupling).
- **Arsitektur Berlapis** + **DIP** (kontrak repository di `interfaces`, implementasi di `infrastructure`).
- **Entity Student**: create, list, get-by-id (in-memory, thread-safe).
- **Health Check**: cepat verifikasi layanan aktif.

---

## 🧱 Arsitektur
```
Presentation  →  Application  →  Domain  →  Infrastructure
(routes, dto)    (services)     (models)     (impl repository)
        ↑               |             |             |
        └───────────────DI (Koin)────┴─────────────┘
```
- **Presentation**: Ktor routes, DTO, StatusPages (tanpa logika bisnis).
- **Application**: service/use-case (orchestrator).
- **Domain**: entity, value object (ID), validasi, exception (murni Kotlin).
- **Infrastructure**: implementasi penyimpanan (InMemory; mudah diganti DB).

---

## 🗂️ Struktur Direktori
```
kotlin-ktor-oop-backend/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradle.properties
├─ gradlew / gradlew.bat
└─ src/
   └─ main/
      ├─ kotlin/
      │  └─ com/academic/
      │     ├─ App.kt
      │     ├─ di/AppModule.kt
      │     ├─ domain/{Exceptions.kt, Ids.kt, Models.kt, Validation.kt}
      │     ├─ application/StudentService.kt
      │     ├─ interfaces/repository/StudentRepository.kt
      │     ├─ infrastructure/repository/InMemoryStudentRepository.kt
      │     └─ presentation/
      │        ├─ dto/StudentDto.kt
      │        ├─ error/StatusHandlers.kt
      │        └─ routes/{HealthRoutes.kt, StudentRoutes.kt}
      └─ resources/application.conf
```

---

## 🧰 Prasyarat
- **JDK 17**
- **IntelliJ IDEA** (disarankan, Community Edition cukup)
- Internet (untuk unduh dependensi Gradle)
- (Opsional) Plugin **HOCON** di IntelliJ untuk highlight `application.conf`

---

## ⚙️ Setup Proyek (Langkah Praktikum – Ramah Pemula)

> **Prinsip penting:** *Kotlin package harus sama dengan struktur folder.* Kita memakai `com.academic` sebagai paket dasar.

### 1) Buat Proyek Gradle (Kotlin DSL)
1. IntelliJ → **New Project** → pilih **Gradle** (centang **Kotlin DSL**), JDK **17**.  
2. **Project name**: `kotlin-ktor-oop-backend` → **Finish**.

### 2) Siapkan Struktur Sumber
1. Buat folder (jika belum ada):
   - `src/main/kotlin`
   - `src/main/resources`
2. Tandai:
   - Klik kanan `src/main/kotlin` → **Mark Directory as → Sources Root**  
   - Klik kanan `src/main/resources` → **Mark Directory as → Resources Root**

### 3) Konfigurasi Gradle (ROOT proyek)
Buat/isi file berikut di **ROOT** (bukan di `src/`):

**`gradle.properties`**
```properties
kotlinVersion=1.9.23
ktorVersion=2.3.12
koinVersion=3.5.3
logbackVersion=1.4.14
kotlinxSerializationVersion=1.6.3
```

**`settings.gradle.kts`**
```kotlin
rootProject.name = "kotlin-ktor-oop-backend"
```

**`build.gradle.kts`**
```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val ktorVersion: String by project
val koinVersion: String by project
val logbackVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "com.academic"
version = "1.0.0"

application {
    mainClass.set("com.academic.AppKt")
}

repositories { mavenCentral() }

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Test (opsional)
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
```

> **Setelah mengedit Gradle**: klik **Reload/Sync Gradle** di IntelliJ.

### 4) Konfigurasi Ktor
**`src/main/resources/application.conf`**
```hocon
ktor {
  deployment { port = 8080 }
  application { modules = [ com.academic.AppKt.module ] }
}
```

### 5) Paket Dasar & Entry Point
Buat paket `com.academic` di `src/main/kotlin`, lalu buat `App.kt`:

```kotlin
package com.academic

import com.academic.di.appModule
import com.academic.presentation.error.configureStatusPages
import com.academic.presentation.routes.configureHealthRoutes
import com.academic.presentation.routes.configureStudentRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(StatusPages) { configureStatusPages() }

    routing {
        configureHealthRoutes()
        configureStudentRoutes()
    }
}
```

### 6) Dependency Injection (Koin)
**`src/main/kotlin/com/academic/di/AppModule.kt`**
```kotlin
package com.academic.di

import com.academic.application.StudentService
import com.academic.infrastructure.repository.InMemoryStudentRepository
import com.academic.interfaces.repository.StudentRepository
import org.koin.dsl.module

val appModule = module {
    single<StudentRepository> { InMemoryStudentRepository() }
    single { StudentService(get()) }
}
```

### 7) Domain Layer
Buat folder `com/academic/domain` dan file-file berikut:

**`Exceptions.kt`**
```kotlin
package com.academic.domain

class ValidationException(message: String) : IllegalArgumentException(message)
class NotFoundException(resource: String, id: String) : Exception("$resource with ID '$id' not found.")
class AlreadyExistsException(message: String) : Exception(message)
class BusinessRuleException(message: String) : Exception(message)
```

**`Ids.kt`**
```kotlin
package com.academic.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline @Serializable value class StudentId(val value: String = UUID.randomUUID().toString())
@JvmInline @Serializable value class CourseId(val value: String = UUID.randomUUID().toString())
@JvmInline @Serializable value class EnrollmentId(val value: String = UUID.randomUUID().toString())
```

**`Validation.kt`**
```kotlin
package com.academic.domain

private val EMAIL_REGEX = "^[A-Za-z0-9](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)
```

**`Models.kt`**
```kotlin
package com.academic.domain

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
```

### 8) Interfaces (Kontrak Repository)
**`src/main/kotlin/com/academic/interfaces/repository/StudentRepository.kt`**
```kotlin
package com.academic.interfaces.repository

import com.academic.domain.Student
import com.academic.domain.StudentId

interface StudentRepository {
    suspend fun save(student: Student): Student
    suspend fun findById(id: StudentId): Student?
    suspend fun findByEmail(email: String): Student?
    suspend fun findAll(): List<Student>
}
```

### 9) Infrastructure (Implementasi In-Memory)
**`src/main/kotlin/com/academic/infrastructure/repository/InMemoryStudentRepository.kt`**
```kotlin
package com.academic.infrastructure.repository

import com.academic.domain.Student
import com.academic.domain.StudentId
import com.academic.interfaces.repository.StudentRepository
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
```

### 10) Application (Service)
**`src/main/kotlin/com/academic/application/StudentService.kt`**
```kotlin
package com.academic.application

import com.academic.domain.Student
import com.academic.domain.StudentId
import com.academic.domain.ValidationException
import com.academic.interfaces.repository.StudentRepository

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
```

### 11) Presentation (DTO & Error)
**`src/main/kotlin/com/academic/presentation/dto/StudentDto.kt`**
```kotlin
package com.academic.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudentRequest(val name: String, val email: String)

@Serializable
data class StudentResponse(val id: String, val name: String, val email: String)
```

**`src/main/kotlin/com/academic/presentation/error/StatusHandlers.kt`**
```kotlin
package com.academic.presentation.error

import com.academic.domain.AlreadyExistsException
import com.academic.domain.BusinessRuleException
import com.academic.domain.NotFoundException
import com.academic.domain.ValidationException
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
```

### 12) Presentation (Routes)
**`src/main/kotlin/com/academic/presentation/routes/HealthRoutes.kt`**
```kotlin
package com.academic.presentation.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureHealthRoutes() {
    route("/health") {
        get { call.respond(mapOf("status" to "UP")) }
    }
}
```

**`src/main/kotlin/com/academic/presentation/routes/StudentRoutes.kt`**
```kotlin
package com.academic.presentation.routes

import com.academic.application.StudentService
import com.academic.domain.StudentId
import com.academic.presentation.dto.CreateStudentRequest
import com.academic.presentation.dto.StudentResponse
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
            call.respond(
                HttpStatusCode.Created,
                StudentResponse(created.id.value, created.name, created.email)
            )
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
```

---

## ▶️ Menjalankan Aplikasi
```bash
# dari root proyek
./gradlew run        # macOS/Linux
# atau
gradlew.bat run      # Windows
```
Aplikasi aktif di `http://localhost:8080`.

---

## 🔌 Uji Endpoint API

### Browser (GET cepat)
- `http://localhost:8080/health` → `{"status":"UP"}`
- `http://localhost:8080/students`

### curl (Terminal)
```bash
# Health
curl http://localhost:8080/health

# Create
curl -X POST http://localhost:8080/students \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'

# List
curl http://localhost:8080/students

# Get by id
curl http://localhost:8080/students/<ID>
```

### IntelliJ HTTP Client
Buat file `api-test.http` di root:
```http
### Health
GET http://localhost:8080/health

### Create
POST http://localhost:8080/students
Content-Type: application/json

{
  "name": "Alice",
  "email": "alice@example.com"
}

### List
GET http://localhost:8080/students
```

### Postman
Buat 4 request seperti di atas (GET/POST). Pastikan header `Content-Type: application/json` untuk POST.

---

## 🩺 Troubleshooting

- **Import `io.ktor.*` / `org.koin.*` merah**  
  Pastikan file Gradle di **root**, lalu **Reload/Sync Gradle**. Cek **Project SDK = 17**. Jika perlu: *Invalidate Caches / Restart*.

- **`Cannot get non-null property 'ktorVersion'`**  
  Gunakan key **tanpa titik** di `gradle.properties` (lihat contoh).

- **`Connection refused`**  
  Pastikan server running & port tidak bentrok (ubah `deployment.port` jika perlu).

- **`415 Unsupported Media Type` saat POST**  
  Tambah header `Content-Type: application/json`.

- **Paket/struktur salah (mis. `package main.kotlin...`)**  
  Pindahkan kode ke `src/main/kotlin/com/academic/...` dan sesuaikan deklarasi `package`.

---

## 🗺️ Roadmap
- Tambah **Course** & **Enrollment** (DTO, routes, service, repository).
- Idempotensi & validasi lanjutan.
- Persistensi **PostgreSQL** (HikariCP + Exposed/Jooq) & switch binding Koin ke Postgres.
- Testing (unit & integration) dengan Ktor Test Engine.
- CI GitHub Actions (build & test).

---

## 🤝 Kontribusi
Pull request dan issue sangat diterima. Ikuti gaya kode Kotlin standar dan struktur berlapis pada repo ini.

---

## 📄 Lisensi
Pilih lisensi Anda (contoh):

```
MIT License © 2025 <Nama Anda/Institusi>
```
