package com.n0n5ense.model.json

import CardId
import CardIdType
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
    val cardId: CardId,
    val accept: Boolean,
    val time: String,
)

@Serializable
data class Count(
    val count: Long
)

@Serializable
data class NewTouchCard(
    val name: String,
    val cardId: CardId,
    val enabled: Boolean,
    val owner: String? = null
)

@Serializable
data class EditTouchCard(
    val name: String?,
    val enabled: Boolean?
)

@Serializable
data class TouchCard(
    val id: Int,
    val name: String,
    val cardId: CardId,
    val enabled: Boolean,
    val owner: String?
)

@Serializable
data class ReaderDeviceInfo(
    val type: String,
    val connected: Boolean,
    val name: String
)

@Serializable
data class EscapeMode(
    val enable: Boolean
)