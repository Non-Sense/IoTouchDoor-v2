package com.n0n5ense.model.json

import kotlinx.serialization.Serializable

enum class UserRole {
    None,
    User,
    Moderator,
    Admin,
}

@Serializable
data class RegisterUser(
    val id: String,
    val name: String,
    val password: String,
    val role: UserRole = UserRole.None,
)

@Serializable
data class LoginUser(
    val id: String,
    val password: String,
)