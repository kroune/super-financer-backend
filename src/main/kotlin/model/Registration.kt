package model

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationBody(
    val login: String,
    val password: String
)