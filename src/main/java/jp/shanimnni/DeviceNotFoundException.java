package jp.shanimnni;

import java.io.Serial;

public class DeviceNotFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = -5957377230306734735L;

    public DeviceNotFoundException() {
        super();
    }

    public DeviceNotFoundException(String msg) {
        super(msg);
    }
}
