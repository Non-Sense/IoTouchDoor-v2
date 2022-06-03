package com.n0n5ense.door

import com.n0n5ense.model.PhysicalLogAction
import com.n0n5ense.persistence.TouchCardService
import com.n0n5ense.persistence.TouchLogService

class DoorService {
    companion object {
        private var door: Door? = null
        var onActionCallback: ((PhysicalLogAction) -> Unit)? = null

        fun init(door: Door) {
            this.door = door
            door.onClose = { onActionCallback?.invoke(PhysicalLogAction.Close) }
            door.onOpen = { onActionCallback?.invoke(PhysicalLogAction.Open) }
            door.onUnlock = { onActionCallback?.invoke(PhysicalLogAction.Unlock) }
            door.onLock = { onActionCallback?.invoke(PhysicalLogAction.Lock) }
        }

        fun lock(force: Boolean = false) {
            door?.lock(force)
        }

        fun unlock() {
            door?.unlock()
        }

        fun status() = door?.getStatus()

        fun addTouchLog(id: String) {
            val cardId = CardId.determineType(id)
            val accept = TouchCardService.find(cardId.id)?.enabled == true
            TouchLogService.add(id, accept)
            if(accept)
                unlock()
        }
    }
}