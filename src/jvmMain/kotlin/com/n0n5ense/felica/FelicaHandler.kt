package com.n0n5ense.felica

import com.n0n5ense.model.FelicaId
import com.n0n5ense.persistence.TouchCardService
import com.n0n5ense.persistence.TouchLogService

class FelicaHandler {
    private var felicaReader: FelicaReader? = null

    var onAccepted: (()->Unit)? = null
    var onError: ((Throwable)->Unit)? = null

    fun init(){
        close()
        felicaReader = FelicaReader()
        felicaReader?.onTouch = ::onCardTouch
        felicaReader?.onError = ::onError
        felicaReader?.open()
    }

    fun close() {
        felicaReader?.close()
        felicaReader = null
    }

    private fun onError(throwable: Throwable){
        close()
        onError?.invoke(throwable)
    }

    private fun onCardTouch(felicaId: FelicaId){
        val accept = TouchCardService.find(felicaId.idm)?.enabled == true
        TouchLogService.add(felicaId.idm, accept)
        if(accept){
            onAccepted?.invoke()
        }
    }
}