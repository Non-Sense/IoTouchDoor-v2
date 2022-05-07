package com.n0n5ense.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object UserTable : Table("user") {
    val id = varchar("id", 32).primaryKey()
    val name = varchar("name", 32)
    val password = varchar("password", 60)
    val enabled = bool("enabled").default(true)
}

@Serializable
data class RegisterUser(
    val id: String,
    val name: String,
    val password: String
)

@Serializable
data class LoginUser(
    val id: String,
    val password: String
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val password: String,
    val enabled: Boolean
)