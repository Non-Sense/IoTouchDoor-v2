package com.n0n5ense.felica

import com.n0n5ense.door.DoorService
import com.n0n5ense.model.FelicaId
import com.n0n5ense.model.json.ReaderDeviceInfo

class FelicaService {
    companion object {
        private var felicaReader: FelicaReader? = null

        var onError: ((Throwable) -> Unit)? = null
        var enabled = false

        fun isConnected(): Boolean {
            return felicaReader != null
        }

        fun open() {
            close()
            felicaReader = FelicaReader()
            felicaReader?.onTouch = ::onCardTouch
            felicaReader?.onError = ::onError
            felicaReader?.open()
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
                enabled,
                name
            )
        }

        private fun onError(throwable: Throwable) {
            close()
            onError?.invoke(throwable)
        }

        private fun onCardTouch(felicaId: FelicaId) {
            DoorService.addTouchLog(felicaId.idm)
        }
    }
}