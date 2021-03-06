package component

import AuthUserContext
import CardId
import CardIdType
import ThemeContext
import com.n0n5ense.model.json.CardTouchLog
import csstype.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import util.getTouchLog
import util.getTouchLogCount
import mui.icons.material.Block
import mui.icons.material.Check
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
import mui.material.*
import mui.material.Size
import mui.system.Breakpoint
import mui.system.Theme
import mui.system.sx
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.img
import react.router.dom.NavLink
import util.value
import kotlin.js.Date

private interface TouchLogRowProps : Props {
    var name: String?
    var cardId: CardId
    var time: String
    var accept: Boolean
    var isXsSize: Boolean
}

private val TouchLogRow = FC<TouchLogRowProps> { props ->
    var open by useState(false)
    Fragment {
        TableRow {
            if (props.isXsSize)
                TableCell {
                    sx {
                        borderBottom = 0.px
                        padding = 0.px
                    }
                    IconButton {
                        size = Size.small
                        ariaLabel = "expand row"
                        onClick = { open = !open }
                        +(if (open) KeyboardArrowUp else KeyboardArrowDown).create()
                    }
                }
            TableCell {
                if (props.isXsSize)
                    sx {
                        borderBottom = 0.px
                    }
                if (props.name != null)
                    +(props.name)!!
                else
                    NavLink {
                        to = "/cards?i=${props.cardId.id}&t=${props.cardId.type.name}"
                        +"Add this"
                    }
            }
            if (!props.isXsSize)
                TableCell {
                    if (props.isXsSize)
                        sx {
                            borderBottom = 0.px
                        }
                    CardIdDisplayBox {
                        this.cardId = props.cardId
                    }
                }
            TableCell {
                if (props.isXsSize)
                    sx {
                        borderBottom = 0.px
                    }
                +(props.time)
            }
            TableCell {
                if (props.isXsSize)
                    sx {
                        borderBottom = 0.px
                    }
                if (props.accept)
                    Check { color = SvgIconColor.success }
                else
                    Block { color = SvgIconColor.error }
            }
        }
        if (props.isXsSize)
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
                        Toolbar {
                            Box {
                                Typography {
                                    sx {
                                        whiteSpace = WhiteSpace.nowrap
                                        marginRight = 8.px
                                    }
                                    variant = "body2"
                                    +"Card ID: "
                                }
                            }
                            CardIdDisplayBox {
                                this.cardId = props.cardId
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
        MainScope().launch {
            getTouchLog(p ?: page, w ?: width, authUser!!)
                .onSuccess {
                    logs = it.data
                    authUser?.accessToken = it.accessToken
                }
        }
    }


    fun updateCount(callback: ((Result<Long>) -> Unit)? = null) {
        MainScope().launch {
            getTouchLogCount(authUser!!)
                .onSuccess {
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
                if (isXsSize)
                    TableCell { }
                TableCell {
                    +"Name"
                    sx {
                        minWidth = 10.ex
                    }
                }
                if (!isXsSize)
                    TableCell {
                        +"Card ID"
                        sx {
                            minWidth = 17.ex
                        }
                    }
                TableCell {
                    +("Time (${js("Intl.DateTimeFormat().resolvedOptions().timeZone")})")
                }
                TableCell {
                    +"Accept"
                }
            }
            TableBody {
                logs.forEach { log ->
                    TouchLogRow {
                        this.isXsSize = isXsSize
                        name = log.name
                        cardId = log.cardId
                        val date = Date(log.time).let { d ->
                            Date(d.getTime() - d.getTimezoneOffset() * 60000)
                        }
                        time = date.toLocaleString("ja-JP")
                        accept = log.accept
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
