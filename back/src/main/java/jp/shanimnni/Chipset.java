package jp.shanimnni;

public class Chipset {

    public static final byte CMD_IN_SET_RF = 0x00;
    public static final byte CMD_IN_SET_PROTOCOL = 0x02;
    public static final byte CMD_IN_COMM_RF = 0x04;
    public static final byte CMD_SWITCH_RF = 0x06;
    public static final byte CMD_MAINTAIN_FLASH = 0x10;
    public static final byte CMD_RESET_DEVICE = 0x12;
    public static final byte CMD_GET_FIRMWARE_VERSION = 0x20;
    public static final byte CMD_GET_PD_DATA_VERSION = 0x22;
    public static final byte CMD_GET_PROPERTY = 0x24;
    public static final byte CMD_IN_GET_PROTOCOL = 0x26;
    public static final byte CMD_GET_COMMAND_TYPE = 0x28;
    public static final byte CMD_SET_COMMAND_TYPE = 0x2A;
    public static final byte CMD_IN_SET_RCT = 0x30;
    public static final byte CMD_IN_GET_RCT = 0x32;
    public static final byte CMD_GET_PD_DATA = 0x34;
    public static final byte CMD_READ_REGISTER = 0x36;
    public static final byte CMD_TG_SET_RF = 0x40;
    public static final byte CMD_TG_SET_PROTOCOL = 0x42;
    public static final byte CMD_TG_SET_AUTO = 0x44;
    public static final byte CMD_TG_SET_RF_OFF = 0x46;
    public static final byte CMD_TG_COMM_RF = 0x48;
    public static final byte CMD_TG_GET_PROTOCOL = 0x50;
    public static final byte CMD_TG_SET_RCT = 0x60;
    public static final byte CMD_TG_GET_RCT = 0x62;
    public static final byte CMD_DIGNOSE = (byte) 0xF0;

}
