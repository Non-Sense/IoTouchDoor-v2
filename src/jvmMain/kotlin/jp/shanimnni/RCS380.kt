package jp.shanimnni

import javax.usb.UsbPipe
import javax.usb.UsbInterface
import javax.usb.UsbException
import java.util.Arrays
import javax.usb.UsbHub
import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import org.apache.commons.lang3.ArrayUtils
import java.nio.ByteBuffer
import javax.usb.UsbConfiguration
import javax.usb.UsbEndpoint
import kotlin.experimental.and

// from https://github.com/nearprosmith/java-rcs380-test
class RCS380 {
    private var pipeIn: UsbPipe
    private var pipeOut: UsbPipe
    private val iface: UsbInterface
    val manufacturer: String
    val productName: String
    @Throws(UsbException::class)
    fun open() {
        iface.claim { usbInterface: UsbInterface? -> true }
        pipeIn.open()
        pipeOut.open()
        pipeOut.syncSubmit(Frame.ACK)
    }

    @Throws(UsbException::class)
    fun close() {
        pipeOut.abortAllSubmissions()
        sendCommand(Chipset.CMD_SWITCH_RF, byteArrayOf(0))
        pipeOut.syncSubmit(Frame.ACK)
        pipeIn.close()
        pipeOut.close()
        iface.release()
    }

    fun sendCommand(commandCode: Byte): ByteBuffer? {
        return this.sendCommand(pipeOut, pipeIn, commandCode, byteArrayOf())
    }

    fun sendCommand(commandCode: Byte, commandData: ByteArray): ByteBuffer? {
        return this.sendCommand(pipeOut, pipeIn, commandCode, commandData)
    }

    @JvmOverloads
    fun sendCommand(
        pipeOut: UsbPipe,
        pipeIn: UsbPipe,
        commandCode: Byte,
        commandData: ByteArray = byteArrayOf()
    ): ByteBuffer? {
        var frame: Frame
        var data = ByteArray(255)
        try {
            frame = Frame(ArrayUtils.addAll(byteArrayOf(0xD6.toByte(), commandCode), *commandData))
            pipeOut.syncSubmit(frame.string)
            pipeIn.syncSubmit(data)
            frame = Frame(data)
            if(frame.type == Frame.TYPE_ACK) {
                data = ByteArray(255)
                pipeIn.syncSubmit(data)
                frame = Frame(data)
                if(frame.data[0] == 0xD7.toByte() && frame.data[1] == (commandCode + 1).toByte()) {
                    return ByteBuffer.wrap(Arrays.copyOfRange(frame.data, 2, frame.data.size))
                }
            } else {
                return null
            }
        } catch(e: UsbException) {
            e.printStackTrace()
            return null
        }
        return null
    }

    fun findDevice(hub: UsbHub?, vendorId: Int, productId: Int): UsbDevice? {
        for(obj in hub!!.attachedUsbDevices) {
            if(obj !is UsbDevice) continue
            var device = obj as UsbDevice?
            val desc = device!!.usbDeviceDescriptor
            if(desc.idVendor().toInt() == vendorId && desc.idProduct().toInt() == productId) return device
            if(device.isUsbHub) {
                device = findDevice(device as UsbHub?, vendorId, productId)
                if(device != null) return device
            }
        }
        return null
    }

    fun setPipeIn(pipeIn: UsbPipe) {
        this.pipeIn = pipeIn
    }

    fun setPipeOut(pipeOut: UsbPipe) {
        this.pipeOut = pipeOut
    }

    companion object {
        const val VENDOR_ID = 0x054C
        const val PRODUCT_ID = 0x06C3

        const val VENDOR_ID2 = 0x054C
        const val PRODUCT_ID2 = 0x06C1
    }

    init {
        val services = UsbHostManager.getUsbServices()
        val rootHub = services.rootUsbHub
        val rcs380 = findDevice(rootHub, VENDOR_ID, PRODUCT_ID)
            ?: findDevice(rootHub, VENDOR_ID2, PRODUCT_ID2)
            ?: throw DeviceNotFoundException("Felica Device Not Found")
        manufacturer = rcs380.manufacturerString
        productName = rcs380.productString
        val configuration = rcs380.usbConfigurations[0] as UsbConfiguration
        iface = configuration.usbInterfaces[0] as UsbInterface
        var endpointOut: UsbEndpoint? = null
        var endpointIn: UsbEndpoint? = null
        for(i in iface.usbEndpoints.indices) {
            val endpointAddr = (iface.usbEndpoints[i] as UsbEndpoint).usbEndpointDescriptor.bEndpointAddress()
            if((endpointAddr and (0x80).toByte()) == (0x80).toByte()) {
                endpointIn = iface.usbEndpoints[i] as UsbEndpoint?
            } else if((endpointAddr and (0x80).toByte()) == (0x00).toByte()) {
                endpointOut = iface.usbEndpoints[i] as UsbEndpoint?
            }
        }
        //0x02 : OUT, 0x081 IN
        endpointOut = iface.getUsbEndpoint(0x02.toByte())
        endpointIn = iface.getUsbEndpoint(0x81.toByte())
        pipeOut = endpointOut.usbPipe
        pipeIn = endpointIn.usbPipe
    }
}