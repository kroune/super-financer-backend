package model

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginBody(
    val login: String,
    val password: String
)