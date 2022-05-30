package component

import AuthUserContext
import com.n0n5ense.model.json.DoorLog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import util.getPhysicalLog
import util.getPhysicalLogCount
import mui.material.*
import react.*
import util.value
import kotlin.js.Date

private interface PhysicalLogRowProps : Props {
    var action: String
    var time: String
}

private val PhysicalLogRow = FC<PhysicalLogRowProps> { props ->
    TableRow {
        TableCell {
            +props.action
        }
        TableCell {
            +props.time
        }
    }
}

val PhysicalLog = FC<Props> { _ ->
    var logs by useState<List<DoorLog>>(listOf())
    val authUser by useContext(AuthUserContext)
    var page by useState(0)
    var width by useState(50)
    var count by useState(0L)

    fun updateLog(p: Int? = null, w: Int? = null) {
        MainScope().launch {
            getPhysicalLog(p ?: page, w ?: width, authUser!!).onSuccess {
                logs = it.data
                authUser?.accessToken = it.accessToken
            }
        }
    }

    fun updateCount(callback: ((Result<Long>) -> Unit)? = null) {
        MainScope().launch {
            getPhysicalLogCount(authUser!!).onSuccess {
                count = it.data.count
                authUser?.accessToken = it.accessToken
            }.map {
                it.data.count
            }.let {
                callback?.invoke(it)
            }
        }
    }

    useEffectOnce {
        updateCount {
            updateLog()
        }
    }

    TableContainer {
        Table {
            TableHead {
                TableCell {
                    +"Action"
                }
                TableCell {
                    +"Time (${js("Intl.DateTimeFormat().resolvedOptions().timeZone")})"
                }
            }
            TableBody {
                logs.forEach { log ->
                    PhysicalLogRow {
                        action = log.action
                        val date = Date(log.time).let { d ->
                            Date(d.getTime() - d.getTimezoneOffset() * 60000)
                        }
                        time = date.toLocaleString("ja-JP")
                    }
                }
            }
            TableFooter {
                TableRow {
                    TablePagination {
                        labelRowsPerPage = ReactNode("")
                        rowsPerPageOptions = arrayOf(25, 50, 100, 200, 500)
                        rowsPerPage = width
                        this.page = page
                        this.count = count
                        onPageChange = { _, p ->
                            val pi = p.toInt()
                            page = pi
                            updateLog(p = pi)
                        }
                        onRowsPerPageChange = {
                            val wi = it.target.value.unsafeCast<Int>()
                            width = wi
                            updateLog(w = wi)
                        }
                    }
                }
            }
        }
    }

}