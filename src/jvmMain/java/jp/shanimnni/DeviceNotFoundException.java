package jp.shanimnni;

public class DeviceNotFoundException extends Exception {

    private static final long serialVersionUID = -5957377230306734735L;

    public DeviceNotFoundException() {
        super();
    }

    public DeviceNotFoundException(String msg) {
        super(msg);
    }
}
