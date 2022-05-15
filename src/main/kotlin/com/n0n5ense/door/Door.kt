package com.n0n5ense.door

import kotlinx.coroutines.*


data class DoorStatus(val isLock: Boolean, val isClose: Boolean)

abstract class Door {

    companion object {
        private const val AUTO_LOCK_TIME: Long = 1000L*10L
        private const val LOCK_TIME: Long = 1000L*3L
        private const val POLLING_INTERVAL: Long = 100L
    }

    init {
        enableAutoLock()
    }

    var onLock: (() -> Unit)? = null
    var onUnlock: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onOpen: (() -> Unit)? = null

    abstract fun unlock()
    abstract fun lock()
    abstract fun getStatus(): DoorStatus?

    private var job: Job? = null

    private fun polling(): () -> Unit {
        var beforeIsLock = true
        var beforeIsClose = true
        var lockOnLongClose = false
        var lastUnlockTime: Long
        var lastCloseTime: Long
        var openFlag = false

        System.currentTimeMillis().let {
            lastCloseTime = it
            lastUnlockTime = it
        }

        return mark@{
            try {
                val status = getStatus() ?: return@mark
                val isLock = status.isLock
                val isClose = status.isClose
                val currentTime = System.currentTimeMillis()
//            log(
//                "polling bl:$beforeIsLock bc:$beforeIsClose l:$isLock c:$isClose at:${currentTime-lastUnlockTime > AUTO_LOCK_TIME} lt:${currentTime-lastCloseTime > LOCK_TIME} op:$openFlag ll:$lockOnLongClose")
                // on unlock
                if(!isLock && beforeIsLock) {
                    log("- unlock")
                    lastUnlockTime = currentTime
                    if(isClose) {
                        onUnlock?.invoke()
                        openFlag = false
                        lockOnLongClose = true
                    }
                }
                // on lock
                if(isLock && !beforeIsLock)
                    onLock?.invoke()
                // on close
                if(isClose && !beforeIsClose)
                    onClose?.invoke()
                // on open
                if(!isClose && beforeIsClose) {
                    log("- open")
                    onOpen?.invoke()
                    lockOnLongClose = false
                    openFlag = true
                }
                if(!isClose) {
                    lastCloseTime = currentTime
                }
                if(isClose && lockOnLongClose && (currentTime - lastUnlockTime > AUTO_LOCK_TIME)) {
                    lockOnLongClose = false
                    log("- longClose")
                    lock()
                }
                if(!isLock && openFlag && (currentTime - lastCloseTime > LOCK_TIME)) {
                    log("- afterClose")
                    lock()
                    openFlag = false
                }
                beforeIsLock = isLock
                beforeIsClose = isClose
            } catch(e: Exception) { }
        }
    }

    private fun log(msg: String){

    }

    fun enableAutoLock() {
        if(isEnableAutoLock())
            return

        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            val f = polling()
            while(true) {
                f.invoke()
                delay(POLLING_INTERVAL)
            }
        }
    }

    fun disableAutoLock() {
        job?.cancel()
        job = null
    }

    fun isEnableAutoLock(): Boolean {
        return job?.isCancelled == false
    }
}
