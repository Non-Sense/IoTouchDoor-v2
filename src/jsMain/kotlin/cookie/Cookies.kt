package cookie

import kotlinext.js.asJsObject
import kotlin.js.Json

private external fun require(name: String): dynamic

private val jsManager = JsManager.init()

private object JsManager {
    fun init() {}

    private val jsCookie = require("js-cookie")


    @Suppress("UnsafeCastFromDynamic")
    fun getConstructor(): Any {
        return jsCookie
    }
}

data class CookieOptions(
    var expires: Int? = null,
    var path: String? = null,
    var domain: String? = null,
    var secure: Boolean? = null
)

class Cookies {
    companion object {
        private val jsCookie = JsManager.getConstructor().asDynamic()

        fun set(name: String, value: String) {
            jsCookie.set(name, value)
        }

        fun set(name: String, value: String, options: CookieOptions) {
            jsCookie.set(name, value, options.asJsObject())
        }

        fun get(name: String, options: CookieOptions): String? {
            return jsCookie.get(name, options.asJsObject()) as? String
        }

        fun get(name: String): String? {
            return jsCookie.get(name) as? String
        }

        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        fun get(): Json {
            return jsCookie.get() as Json
        }

        fun remove(name: String, options: CookieOptions) {
            jsCookie.remove(name, options.asJsObject())
        }

        fun remove(name: String) {
            jsCookie.remove(name)
        }

        fun remove() {
            jsCookie.remove()
        }
    }
}