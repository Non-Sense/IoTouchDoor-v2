package jp.shanimnni

object Chipset {
    const val CMD_IN_SET_RF: Byte = 0x00
    const val CMD_IN_SET_PROTOCOL: Byte = 0x02
    const val CMD_IN_COMM_RF: Byte = 0x04
    const val CMD_SWITCH_RF: Byte = 0x06
    const val CMD_MAINTAIN_FLASH: Byte = 0x10
    const val CMD_RESET_DEVICE: Byte = 0x12
    const val CMD_GET_FIRMWARE_VERSION: Byte = 0x20
    const val CMD_GET_PD_DATA_VERSION: Byte = 0x22
    const val CMD_GET_PROPERTY: Byte = 0x24
    const val CMD_IN_GET_PROTOCOL: Byte = 0x26
    const val CMD_GET_COMMAND_TYPE: Byte = 0x28
    const val CMD_SET_COMMAND_TYPE: Byte = 0x2A
    const val CMD_IN_SET_RCT: Byte = 0x30
    const val CMD_IN_GET_RCT: Byte = 0x32
    const val CMD_GET_PD_DATA: Byte = 0x34
    const val CMD_READ_REGISTER: Byte = 0x36
    const val CMD_TG_SET_RF: Byte = 0x40
    const val CMD_TG_SET_PROTOCOL: Byte = 0x42
    const val CMD_TG_SET_AUTO: Byte = 0x44
    const val CMD_TG_SET_RF_OFF: Byte = 0x46
    const val CMD_TG_COMM_RF: Byte = 0x48
    const val CMD_TG_GET_PROTOCOL: Byte = 0x50
    const val CMD_TG_SET_RCT: Byte = 0x60
    const val CMD_TG_GET_RCT: Byte = 0x62
    const val CMD_DIGNOSE = 0xF0.toByte()
}