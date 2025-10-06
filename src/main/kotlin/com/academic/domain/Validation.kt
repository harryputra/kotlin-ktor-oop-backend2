package org.example.com.academic.domain

private val EMAIL_REGEX = "^[A-Za-z0-9](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)