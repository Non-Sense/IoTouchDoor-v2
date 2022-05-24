package com.n0n5ense.model.json

import kotlinx.serialization.Serializable

@Serializable
data class DoorLockAction(
    val action: String,
    val force: Boolean? = null
)

@Serializable
data class DoorStatus(
    val active: Boolean,
    val isClose: Boolean,
    val isLock: Boolean
)

@Serializable
data class DoorLog(
    val id: Long,
    val action: String,
    val time: String
)

@Serializable
data class CardTouchLog(
    val id: Int,
    val name: String?,
    val cardId: String,
    val accept: Boolean,
    val time: String
)

@Serializable
data class Count(
    val count: Long
)