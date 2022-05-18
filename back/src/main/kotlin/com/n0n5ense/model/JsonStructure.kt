package com.n0n5ense.model

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
){
    companion object {
        fun from(status: com.n0n5ense.door.DoorStatus?): DoorStatus{
            return DoorStatus(
                status != null,
                status?.isClose == true,
                status?.isLock == true
            )
        }
    }
}

@Serializable
data class DoorLog(
    val id: Long,
    val action: String,
    val time: String
)

@Serializable
data class CardTouchLog(
    val id: Int,
    val cardId: String,
    val accept: Boolean,
    val time: String
)