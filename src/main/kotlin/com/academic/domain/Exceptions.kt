package org.example.com.academic.domain


class ValidationException(message: String) : IllegalArgumentException(message)
class NotFoundException(resource: String, id: String) : Exception("$resource with ID '$id' not found.")
class AlreadyExistsException(message: String) : Exception(message)
class BusinessRuleException(message: String) : Exception(message)