package jp.shanimnni

import java.lang.Exception

class DeviceNotFoundException: Exception {
    constructor(): super() {}
    constructor(msg: String?): super(msg) {}

    companion object {
        private const val serialVersionUID = -5957377230306734735L
    }
}