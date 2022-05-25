package component

import AuthUser
import AuthUserContext
import DataWithAccessToken
import com.n0n5ense.model.json.Count
import com.n0n5ense.model.json.DoorLog
import getJsonDataWithTokenRetry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.material.*
import react.*
import serverAddress
import value
import kotlin.js.Date

private interface PhysicalLogRowProps: Props {
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
        getLogs(p ?: page, w ?: width, authUser!!) { result ->
            result.onSuccess {
                logs = it.data
                authUser?.accessToken = it.accessToken
            }
        }
    }

    fun updateCount(callback: ((Result<Long>) -> Unit)? = null) {
        getLogCount(authUser!!) { result ->
            result.onSuccess {
                count = it.data.count
                authUser?.accessToken = it.accessToken
                callback?.invoke(Result.success(it.data.count))
            }.onFailure {
                callback?.invoke(Result.failure(it))
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
                            Date(d.getTime() - d.getTimezoneOffset()*60000)
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

private fun getLogs(
    page: Int,
    width: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<List<DoorLog>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/door/log?p=$page&w=$width", authUser))
    }
}

private fun getLogCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/door/logcount", authUser))
    }
}