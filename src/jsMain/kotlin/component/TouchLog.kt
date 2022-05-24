package component

import AuthUser
import AuthUserContext
import DataWithAccessToken
import ThemeContext
import com.n0n5ense.model.json.CardTouchLog
import com.n0n5ense.model.json.Count
import csstype.Display
import csstype.Visibility
import csstype.ex
import csstype.px
import getJsonDataWithTokenRetry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.js.jso
import mui.icons.material.Block
import mui.icons.material.Check
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
import mui.material.*
import mui.system.Breakpoint
import mui.system.Breakpoints
import mui.system.Theme
import mui.system.sx
import react.*
import react.dom.aria.ariaColSpan
import react.dom.aria.ariaLabel
import react.dom.aria.ariaMultiline
import serverAddress
import value
import kotlin.js.Date

private interface RowProps: Props {
    var name: String
    var cardId: String
    var time: String
    var accept: Boolean
    var isXsSize: Boolean
}

private val Row = FC<RowProps>{ props ->
    var open by useState(false)
    TableRow {
        if(props.isXsSize)
            TableCell {
                IconButton {
                    size = Size.small
                    ariaLabel = "expand row"
                    onClick = { open = !open }
                    +(if(open) KeyboardArrowUp else KeyboardArrowDown).create()
                }
            }
        TableCell {
            +(props.name)
        }
        if(!props.isXsSize)
            TableCell {
                +(props.cardId)
            }
        TableCell {
            +(props.time)
        }
        TableCell {
            if(props.accept)
                Check { color = SvgIconColor.success }
            else
                Block { color = SvgIconColor.error }
        }
    }
    if(props.isXsSize)
        TableRow {
            TableCell {
                sx {
                    paddingBottom = 0.px
                    paddingTop = 0.px
                }
                colSpan = 4
                Collapse {
                    `in` = open
                    timeout = "auto"
                    Box {
                        sx {
                            margin = 1.px
                        }
                        Grid {
                            container = true
                            Grid {
                                item = true
                                xs = 6
                                Typography {
                                    +"Card ID: "
                                }
                            }
                            Grid {
                                item = true
                                xs = 6
                                Typography {
                                    +props.cardId
                                }
                            }
                        }
                    }
                }
            }
        }
}

val TouchLog = FC<Props> { _ ->
    var logs by useState<List<CardTouchLog>>(listOf())
    val authUser by useContext(AuthUserContext)
    var page by useState(0)
    var width by useState(50)
    var count by useState(0L)

    var theme by useContext(ThemeContext)
    val isXsSize = useMediaQuery<Theme>(queryInput = {
        it.breakpoints.down(Breakpoint.sm)
    })

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

    Button {
        +"Get"
        onClick = {
            updateCount()
            updateLog()
        }
    }
    TableContainer {
        Table {
            TableHead {
                if(isXsSize)
                    TableCell { }
                TableCell {
                    +"Name"
                    sx {
                        minWidth = 10.ex
                    }
                }
                if(!isXsSize)
                    TableCell {
                        +"Card ID"
                        sx {
                            minWidth = 17.ex
                        }
                    }
                TableCell {
                    +("Time(${js("Intl.DateTimeFormat().resolvedOptions().timeZone")})")
                }
                TableCell {
                    +"Accept"
                }
            }
            TableBody {
                logs.map { log ->
                    Row {
                        this.isXsSize = isXsSize
                        name = log.name?:""
                        cardId = log.cardId
                        val date = Date(log.time).let { d ->
                            Date(d.getTime()-d.getTimezoneOffset()*60000)
                        }
                        time = date.toLocaleString("ja-JP")
                        accept = log.accept
                    }
                }
//                logs.map {
//                    TableRow {
//                        if(isXsSize)
//                            TableCell {
//                                IconButton {
//                                    size = Size.small
//                                    ariaLabel = "expand row"
//                                }
//                            }
//                        TableCell {
//                            +(it.name ?: "")
//                        }
//                        if(!isXsSize)
//                            TableCell {
//                                +(it.cardId)
//                            }
//                        TableCell {
//                            val date = Date(it.time).let { d ->
//                                Date(d.getTime()-d.getTimezoneOffset()*60000)
//                            }
//                            +(date.toLocaleString("ja-JP"))
//                        }
//                        TableCell {
//                            if(it.accept)
//                                Check { color = SvgIconColor.success }
//                            else
//                                Block { color = SvgIconColor.error }
//                        }
//                    }
//                }
            }
            TableFooter {
                TableRow {
                    TablePagination {
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
    callback: ((Result<DataWithAccessToken<List<CardTouchLog>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/log?p=$page&w=$width", authUser))
    }
}

private fun getLogCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/logcount", authUser))
    }
}