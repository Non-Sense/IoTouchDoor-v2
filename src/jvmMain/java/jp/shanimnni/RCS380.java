package jp.shanimnni;

// from https://github.com/nearprosmith/java-rcs380-test

import org.apache.commons.lang3.ArrayUtils;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RCS380 {
    static int VENDOR_ID = 0x054C;
    static int PRODUCT_ID = 0x06C3;
    private UsbPipe pipeIn;
    private UsbPipe pipeOut;
    private final UsbInterface iface;
    private final String manufacturer;
    private final String productName;

    public RCS380() throws UsbException, UnsupportedEncodingException, DeviceNotFoundException {
        UsbServices services = UsbHostManager.getUsbServices();

        UsbHub rootHub = services.getRootUsbHub();
        UsbDevice rcs380 = this.findDevice(rootHub, RCS380.VENDOR_ID, RCS380.PRODUCT_ID);

        if (rcs380 == null)
            throw new DeviceNotFoundException("Felica Device Not Found");

        this.manufacturer = rcs380.getManufacturerString();
        this.productName = rcs380.getProductString();

        UsbConfiguration configuration = (UsbConfiguration) rcs380.getUsbConfigurations().get(0);
        this.iface = (UsbInterface) configuration.getUsbInterfaces().get(0);


        UsbEndpoint endpointOut = null, endpointIn = null;
        for (int i = 0; i < iface.getUsbEndpoints().size(); i++) {
            byte endpointAddr = (byte) ((UsbEndpoint) (iface.getUsbEndpoints().get(i))).getUsbEndpointDescriptor().bEndpointAddress();
            if (((endpointAddr & 0x80) == 0x80)) {
                endpointIn = (UsbEndpoint) (iface.getUsbEndpoints().get(i));
            } else if ((endpointAddr & 0x80) == 0x00) {
                endpointOut = (UsbEndpoint) (iface.getUsbEndpoints().get(i))
                ;
            }
        }
        //0x02 : OUT, 0x081 IN
        endpointOut = iface.getUsbEndpoint((byte) 0x02);
        endpointIn = iface.getUsbEndpoint((byte) 0x81);

        this.pipeOut = endpointOut.getUsbPipe();
        this.pipeIn = endpointIn.getUsbPipe();
    }

    public void open() throws UsbException {
        this.iface.claim(usbInterface -> true);
        this.pipeIn.open();
        this.pipeOut.open();
        this.pipeOut.syncSubmit(Frame.ACK);
    }

    public void close() throws UsbException {
        this.pipeOut.abortAllSubmissions();
        sendCommand(Chipset.CMD_SWITCH_RF, new byte[]{0});
        this.pipeOut.syncSubmit(Frame.ACK);
        this.pipeIn.close();
        this.pipeOut.close();
        this.iface.release();
    }


    public ByteBuffer sendCommand(byte commandCode) {
        return this.sendCommand(this.pipeOut, this.pipeIn, commandCode, new byte[]{});
    }

    public ByteBuffer sendCommand(byte commandCode, byte[] commandData) {
        return this.sendCommand(this.pipeOut, this.pipeIn, commandCode, commandData);
    }

    public ByteBuffer sendCommand(UsbPipe pipeOut, UsbPipe pipeIn, byte commandCode) {
        return this.sendCommand(pipeOut, pipeIn, commandCode, new byte[]{});
    }

    public ByteBuffer sendCommand(UsbPipe pipeOut, UsbPipe pipeIn, byte commandCode, byte[] commandData) {
        Frame frame;
        byte[] data = new byte[255];
        try {
            frame = new Frame(ArrayUtils.addAll(new byte[]{(byte) 0xD6, commandCode}, commandData));
            pipeOut.syncSubmit(frame.frame);
            pipeIn.syncSubmit(data);
            frame = new Frame(data);

            if (frame.type.equals(Frame.TYPE_ACK)) {
                data = new byte[255];

                pipeIn.syncSubmit(data);
                frame = new Frame(data);
                if (frame.data[0] == (byte) 0xD7 && frame.data[1] == commandCode + 1) {
                    return ByteBuffer.wrap(Arrays.copyOfRange(frame.data, 2, frame.data.length));
                }
            } else {
                return null;
            }
        } catch (UsbException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public UsbDevice findDevice(UsbHub hub, int vendorId, int productId) {
        for (Object obj : hub.getAttachedUsbDevices()) {
            if (!(obj instanceof UsbDevice))
                continue;
            UsbDevice device = (UsbDevice)obj;
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();

            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

    public void setPipeIn(UsbPipe pipeIn) {
        this.pipeIn = pipeIn;
    }

    public void setPipeOut(UsbPipe pipeOut) {
        this.pipeOut = pipeOut;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getProductName() {
        return productName;
    }
}

