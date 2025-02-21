package model

data class JwtTokenBody(
    val login: String,
    val password: String,
    val userId: Long
)