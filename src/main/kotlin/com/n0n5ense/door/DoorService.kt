package com.n0n5ense.door

import com.n0n5ense.model.PhysicalLogAction

class DoorService {
    companion object{
        private var door: Door? = null
        var onActionCallback: ((PhysicalLogAction)->Unit)? = null

        fun init(door: Door){
            this.door = door
            door.onClose = { onActionCallback?.invoke(PhysicalLogAction.Close) }
            door.onOpen = { onActionCallback?.invoke(PhysicalLogAction.Open) }
            door.onUnlock = { onActionCallback?.invoke(PhysicalLogAction.Unlock) }
            door.onLock = { onActionCallback?.invoke(PhysicalLogAction.Lock) }
        }

        fun lock() {
            door?.lock()
        }

        fun unlock() {
            door?.unlock()
        }

        fun status() = door?.getStatus()
    }
}