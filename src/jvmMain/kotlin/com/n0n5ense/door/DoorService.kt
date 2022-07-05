package com.n0n5ense.door

import com.n0n5ense.model.PhysicalLogAction
import com.n0n5ense.persistence.TouchCardService
import com.n0n5ense.persistence.TouchLogService
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

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
            door.enableAutoLock()
        }

        fun lock(force: Boolean = false) {
            door?.lock(force)
        }

        fun unlock() {
            door?.unlock()
        }

        private var escapeModeForceCancelJob: Job? = null
        fun setEscapeMode(enable: Boolean) {
            door?.setEscapeMode(enable)
            if(enable && door?.getEscapeMode() != true)
                startEscapeModeCancelTimer()
            if(!enable)
                cancelEscapeModeCancelTimer()
        }

        private fun startEscapeModeCancelTimer() {
            cancelEscapeModeCancelTimer()
            escapeModeForceCancelJob = CoroutineScope(Dispatchers.Default).launch {
                delay(45.minutes)
                if(isActive)
                    setEscapeMode(false)
            }
        }

        private fun cancelEscapeModeCancelTimer() {
            escapeModeForceCancelJob?.cancel()
            escapeModeForceCancelJob = null
        }

        fun getEscapeMode(): Boolean {
            return door?.getEscapeMode() == true
        }

        fun status() = door?.getStatus()

        fun addTouchLog(id: String) {
            val cardId = CardId.determineType(id)
            val accept = TouchCardService.find(cardId)?.enabled == true
            TouchLogService.add(cardId, accept)
            if(accept)
                unlock()
        }
    }
}