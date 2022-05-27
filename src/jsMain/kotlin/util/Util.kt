package util

import org.w3c.dom.events.EventTarget
import kotlin.js.Date

interface ValueInterface {
    var value: String
}

val EventTarget.value: String
    get() {
        return this.unsafeCast<ValueInterface>().value
    }

fun toSortDatetimeString(time: String): String {
    val date = Date(time).let { d ->
        Date(d.getTime() - d.getTimezoneOffset()*60000)
    }
    val m = (date.getMonth()+1).toString().padStart(2, '0')
    val d = date.getDate().toString().padStart(2, '0')
    val h = date.getHours().toString().padStart(2, '0')
    val min = date.getMinutes().toString().padStart(2, '0')
    return "$m/$d $h:$min"
}