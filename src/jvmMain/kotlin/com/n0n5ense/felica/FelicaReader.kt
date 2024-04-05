package com.n0n5ense.felica

import com.n0n5ense.model.FelicaId
import jp.shanimnni.Chipset
import jp.shanimnni.RCS380
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.*

class FelicaReader {

    private lateinit var reader: RCS380
    var firmwareVersion: String? = null
        private set
    private var pdDataVersion: String? = null

    private var job: Job? = null

    var onTouch: ((FelicaId) -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null

    var manufacturer: String? = null
    var productName: String? = null

    fun open(): Result<Unit> = kotlin.runCatching {
        reader = RCS380()
        reader.open()
        manufacturer = reader.manufacturer
        productName = reader.productName
        preCommand()
        start()
    }

    fun close() {
        job?.cancel()
        job = null
        if(::reader.isInitialized) {
            kotlin.runCatching {
                reader.close()
            }
        }
    }

    private fun preCommand() {
        reader.sendCommand(Chipset.CMD_SET_COMMAND_TYPE, byteArrayOf(0x01))
        reader.sendCommand(Chipset.CMD_GET_FIRMWARE_VERSION)?.let {
            firmwareVersion = "%d.%02d".format(it.get(1), it.get(0))
        }
        reader.sendCommand(Chipset.CMD_GET_PD_DATA_VERSION)?.let {
            pdDataVersion = "%d.%02d".format(it.get(1), it.get(0))
        }
        reader.sendCommand(Chipset.CMD_SWITCH_RF, byteArrayOf(0x00))

        //0x01010f01 : F
        //0x02030f03 : A
        //0x03070f07 : B
        reader.sendCommand(Chipset.CMD_IN_SET_RF, byteArrayOf(0x01, 0x01, 0x0f, 0x01))
        reader.sendCommand(
            Chipset.CMD_IN_SET_PROTOCOL,
            byteArrayOf(
                0x00, 0x18, 0x01, 0x01, 0x02, 0x01, 0x03, 0x00,
                0x04, 0x00, 0x05, 0x00, 0x06, 0x00, 0x07, 0x08,
                0x08, 0x00, 0x09, 0x00, 0x0a, 0x00, 0x0b, 0x00,
                0x0c, 0x00, 0x0e, 0x04, 0x0f, 0x00, 0x10, 0x00,
                0x11, 0x00, 0x12, 0x00, 0x13, 0x06
            )
        )
        reader.sendCommand(Chipset.CMD_IN_SET_PROTOCOL, byteArrayOf(0x00, 0x18))
    }

    private fun start() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                kotlin.runCatching {
                    proc()
                }.onFailure {
                    onError?.invoke(it)
                    close()
                }
                delay(250)
            }
        }
    }

    private var idm = 0L
    private fun proc() {
        val buf = reader.sendCommand(
            Chipset.CMD_IN_COMM_RF,
            byteArrayOf(0x6e, 0x00, 0x06, 0x00, 0xff.toByte(), 0xff.toByte(), 0x01, 0x00)
        ) ?: return
        if(buf.array().contentEquals(byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x00))) {
            idm = 0L
            return
        }
        //Type-F
        if(buf.remaining() > 6) {
            if(buf.get(5).toInt() == 0x14 && buf.get(6).toInt() == 0x01) {
                val currentIdm = ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), 7, 15)).long
                if(currentIdm == idm)
                    return
                idm = currentIdm
                val pmm = ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), 15, 23)).long
                onTouch?.invoke(FelicaId(String.format("%016X", idm), String.format("%016X", pmm)))
            }
        }
    }

}