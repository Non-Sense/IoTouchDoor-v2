package com.n0n5ense.magnetic

import com.n0n5ense.door.DoorService
import com.n0n5ense.model.json.ReaderDeviceInfo
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.ByteBuffer

private data class Event(
    val time: Long,
    val nano: Long,
    val type: Short,
    val code: Short,
    val value: Int
)

class MagneticReader {
    companion object {
        private val logger = LoggerFactory.getLogger("MagneticReader")
        private var input: BufferedInputStream? = null
        private var reader: MagReader? = null
        private var job: Job? = null
        private var devicePath: String? = null
        var onTouch: ((String)->Unit)? = null

        var enabled = false
        var onError: ((Throwable) -> Unit)? = null

        fun open(path: String): Result<Unit> {
            devicePath = path
            return kotlin.runCatching {
                input = BufferedInputStream(FileInputStream(path))
                reader = MagReader()
                reader?.onInputEnterCallback = Companion::onEnter
                startJob()
            }.onFailure {
                logger.error("error on open ${it.stackTraceToString()}")
                close()
            }
        }

        private fun onError(throwable: Throwable) {
            onError?.invoke(throwable)
        }

        fun close() {
            logger.info("mag close")
            kotlin.runCatching {
                input?.close()
            }
            kotlin.runCatching {
                job?.cancel()
            }
            devicePath = null
            job = null
            input = null
            reader = null
        }

        fun getInfo(): ReaderDeviceInfo {
            return ReaderDeviceInfo("Magnetic", devicePath!=null, devicePath?:"")
        }

        private fun startJob() {
            job = CoroutineScope(Dispatchers.Default).launch {
                logger.info("mag job started")
                while (true) {
                    if (reader == null || job == null || job?.isCancelled == true) {
                        break
                    }
                    val input = input
                    if(input == null) {
                        delay(100)
                        continue
                    }

                    val bytes = kotlin.runCatching {
                        val bb = ByteArray(24)
                        repeat(24) {
                            bb[it] = input.read().toByte()
                        }
                        bb.reversed()
                    }.onFailure {
                        logger.error(it.stackTraceToString())
                        job?.cancel()
                        job = null
                        close()
                        onError(it)
                    }.getOrNull() ?: break

                    val event = Event(
                        ByteBuffer.wrap(bytes.slice(16..23).toByteArray()).long,
                        ByteBuffer.wrap(bytes.slice(8..15).toByteArray()).long,
                        ByteBuffer.wrap(bytes.slice(6..7).toByteArray()).short,
                        ByteBuffer.wrap(bytes.slice(4..5).toByteArray()).short,
                        ByteBuffer.wrap(bytes.slice(0..3).toByteArray()).int
                    )
                    reader?.addEvent(event)
                }
                job?.cancel()
                job = null
                close()
            }
        }

        private fun onEnter(id: String) {
            logger.info("magID: $id")
            DoorService.addTouchLog(id)
            onTouch?.invoke(id)
        }
    }
}

private class MagReader {
    companion object {
        private const val LeftShift = (42).toShort()
        private const val RightShift = (54).toShort()
        private const val Enter = (28).toShort()
        private val keyMap = mapOf(
            (30).toShort() to Key('a', 'A'),
            (48).toShort() to Key('b', 'B'),
            (46).toShort() to Key('c', 'C'),
            (32).toShort() to Key('d', 'D'),
            (18).toShort() to Key('e', 'E'),
            (33).toShort() to Key('f', 'F'),
            (34).toShort() to Key('g', 'G'),
            (35).toShort() to Key('h', 'H'),
            (23).toShort() to Key('i', 'I'),
            (36).toShort() to Key('j', 'J'),
            (37).toShort() to Key('k', 'K'),
            (38).toShort() to Key('l', 'L'),
            (50).toShort() to Key('m', 'M'),
            (49).toShort() to Key('n', 'N'),
            (24).toShort() to Key('o', 'O'),
            (25).toShort() to Key('p', 'P'),
            (16).toShort() to Key('q', 'Q'),
            (19).toShort() to Key('r', 'R'),
            (31).toShort() to Key('s', 'S'),
            (20).toShort() to Key('t', 'T'),
            (22).toShort() to Key('u', 'U'),
            (47).toShort() to Key('v', 'V'),
            (17).toShort() to Key('w', 'W'),
            (45).toShort() to Key('x', 'X'),
            (21).toShort() to Key('y', 'Y'),
            (44).toShort() to Key('z', 'Z'),
            (2).toShort() to Key('1', '!'),
            (3).toShort() to Key('2', '@'),
            (4).toShort() to Key('3', '#'),
            (5).toShort() to Key('4', '$'),
            (6).toShort() to Key('5', '%'),
            (7).toShort() to Key('6', '^'),
            (8).toShort() to Key('7', '&'),
            (9).toShort() to Key('8', '*'),
            (10).toShort() to Key('9', '('),
            (11).toShort() to Key('0', ')'),
            (15).toShort() to Key('\t', '\t'),
            (57).toShort() to Key(' ', ' '),
            (12).toShort() to Key('-', '_'),
            (13).toShort() to Key('=', '+'),
            (26).toShort() to Key('[', '{'),
            (27).toShort() to Key(']', '}'),
            (43).toShort() to Key('\\', '|'),
            (39).toShort() to Key(';', ':'),
            (40).toShort() to Key('\'', '"'),
            (41).toShort() to Key('`', '~'),
            (51).toShort() to Key(',', '<'),
            (52).toShort() to Key('.', '>'),
            (53).toShort() to Key('/', '?'),
        )
    }

    private data class Key(
        val char: Char,
        val shiftChar: Char
    )

    private var stringBuilder = StringBuilder()
    private var shift = false
    var onInputEnterCallback: ((String) -> Unit)? = null
    fun addEvent(event: Event) {
        if (event.type != (1).toShort())
            return
        if (event.code == LeftShift || event.code == RightShift) {
            shift = event.value == 1
            return
        }
        if (event.value != 1)
            return
        if (event.code == Enter) {
            onInputEnter()
            return
        }
        keyMap[event.code]?.let {
            stringBuilder.append(if (shift) it.shiftChar else it.char)
        }
    }

    private fun onInputEnter() {
        val str = stringBuilder.toString()
        stringBuilder = StringBuilder()
        onInputEnterCallback?.invoke(str)
    }

}
