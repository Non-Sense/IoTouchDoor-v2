package jp.shanimnni

import java.util.Arrays
import com.igormaznitsa.jbbp.io.JBBPOut
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Frame internal constructor(dataBytes: ByteArray) {
    var data: ByteArray = ByteArray(0)
    var type: String? = null
    var string: ByteArray = ByteArray(0)
    fun getCheckSum(data: ByteArray): Byte {
        val sum = sumOfBytes(data)
        return ((0x100 - sum)%0x100).toByte()
    }

    fun sumOfBytes(bytes: ByteArray): Int {
        var sum = 0
        for(aByte in bytes) {
            sum += 0x0FF and aByte.toInt()
        }
        return sum
    }

    companion object {
        var ACK = byteArrayOf(0x00, 0x00, 0xff.toByte(), 0x00, 0xff.toByte(), 0x00)
        var ERR = byteArrayOf(0x00, 0x00, 0xff.toByte(), 0xff.toByte(), 0xff.toByte())
        var TYPE_ACK = "ACK"
        var TYPE_ERR = "ERR"
        var TYPE_DATA = "DATA"
    }

    init {
        if(Arrays.equals(
                Arrays.copyOfRange(dataBytes, 0, 3),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xff.toByte())
            )
        ) {
            string = dataBytes
            if(Arrays.equals(Arrays.copyOfRange(dataBytes, 0, 6), ACK)) {
                type = TYPE_ACK
                //            } else if (Arrays.equals( Arrays.copyOfRange(data, 0, 5), Frame.ERR)) {
//                this.type = "err";
            } else if(Arrays.equals(Arrays.copyOfRange(string, 3, 5), byteArrayOf(0xff.toByte(), 0xff.toByte()))) {
                type = TYPE_DATA
            }
            assert(type != null)
            if(type == TYPE_DATA) {
                val buf = ByteBuffer.wrap(Arrays.copyOfRange(string, 5, 7))
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val length = buf.short.toInt()
                data = Arrays.copyOfRange(string, 8, 8 + length)
            }
        } else {
            var bytes = JBBPOut.BeginBin()
            try {
                bytes.Byte(0, 0, -1, -1, -1)
                bytes.Byte(
                    ByteBuffer.allocate(java.lang.Short.SIZE/java.lang.Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(dataBytes.size.toShort()).array()
                )
                bytes.Byte(
                    getCheckSum(
                        ByteBuffer.allocate(java.lang.Short.SIZE/java.lang.Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN)
                            .putShort(dataBytes.size.toShort()).array()
                    ).toInt()
                )
                bytes.Byte(dataBytes)
                val cur = bytes.End().toByteArray()
                bytes = JBBPOut.BeginBin().Byte(cur)
                bytes.Byte(getCheckSum(Arrays.copyOfRange(cur, 8, cur.size)).toInt())
                bytes.Byte(0)
                string = bytes.End().toByteArray()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}