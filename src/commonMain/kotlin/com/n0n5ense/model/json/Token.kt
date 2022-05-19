package com.n0n5ense.model.json

import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(
    val accessToken: String
)

@Serializable
data class RefreshToken(
    val refreshToken: String
)