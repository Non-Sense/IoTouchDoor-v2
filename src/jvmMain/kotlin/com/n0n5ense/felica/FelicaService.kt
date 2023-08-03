package com.n0n5ense.felica

import com.n0n5ense.door.DoorService
import com.n0n5ense.model.FelicaId
import com.n0n5ense.model.json.ReaderDeviceInfo

class FelicaService {
    companion object {
        private var felicaReader: FelicaReader? = null

        var onError: ((Throwable) -> Unit)? = null
        var enabled = false
        var onTouch: ((FelicaId) -> Unit)? = null

        fun isConnected(): Boolean {
            return felicaReader != null
        }

        fun open(): Result<Unit> {
            return kotlin.runCatching {
                close()
                felicaReader = FelicaReader()
                felicaReader!!.onTouch = ::onCardTouch
                felicaReader!!.onError = ::onError
                felicaReader!!.open().getOrThrow()
            }.onFailure { felicaReader = null }
        }

        fun close() {
            kotlin.runCatching {
                felicaReader?.close()
            }
            felicaReader = null
        }

        fun getInfo(): ReaderDeviceInfo {
            val name = felicaReader?.let {
                "${it.manufacturer ?: ""} ${it.productName ?: ""} ${it.firmwareVersion ?: ""}"
            } ?: ""
            return ReaderDeviceInfo(
                "Felica",
                felicaReader != null,
                name
            )
        }

        private fun onError(throwable: Throwable) {
            close()
            onError?.invoke(throwable)
        }

        private fun onCardTouch(felicaId: FelicaId) {
            DoorService.addTouchLog(felicaId.idm)
            onTouch?.invoke(felicaId)
        }
    }
}