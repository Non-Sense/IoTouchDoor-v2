package component

import AuthUserContext
import com.n0n5ense.model.json.CardTouchLog
import com.n0n5ense.model.json.DoorLog
import csstype.JustifyContent
import csstype.number
import csstype.px
import kotlinx.js.timers.setInterval
import mui.icons.material.Block
import mui.icons.material.Check
import mui.icons.material.Sync
import mui.material.*
import mui.system.ResponsiveStyleValue
import mui.system.sx
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML
import react.router.dom.NavLink
import util.getPhysicalLog
import util.getTouchLog
import util.toSortDatetimeString
import kotlin.time.Duration.Companion.milliseconds

private val dashboardFetchInterval = 5000.milliseconds

val DashboardPage = FC<Props> {
    Container {
        sx {
            marginTop = 8.px
        }
        DashBoard()
    }
}

val DashBoard = FC<Props> {

    val authUser by useContext(AuthUserContext)
    var touchLog by useState<List<CardTouchLog>>(listOf())
    var physicalLog by useState<List<DoorLog>>(listOf())

    fun fetch() {
        getTouchLog(0,5,authUser!!) { result ->
            result.onSuccess {
                authUser?.accessToken = it.accessToken
                touchLog = it.data
                getPhysicalLog(0,5,authUser!!) { result2 ->
                    result2.onSuccess { d2 ->
                        authUser?.accessToken = d2.accessToken
                        physicalLog = d2.data
                    }
                }
            }
        }
    }

    useEffectOnce {
        fetch()
        setInterval(dashboardFetchInterval) {
            fetch()
        }
    }

    Grid {
        container = true
        spacing = ResponsiveStyleValue(4)
        sx {
            justifyContent = JustifyContent.center
        }
        Grid {
            item = true
            xs = 12
            sm = 10
            md = 6
            MiniTouchLog {
                this.log = touchLog
                this.onClickRefresh = {
                    getTouchLog(0, 5, authUser!!) { res ->
                        res.onSuccess {
                            authUser?.accessToken = it.accessToken
                            touchLog = it.data
                        }
                    }
                }
            }
        }
        Grid {
            item = true
            xs = 12
            sm = 10
            md = 6
            MiniPhysicalLog {
                this.log = physicalLog
                this.onClickRefresh = {
                    getPhysicalLog(0,5,authUser!!) { res ->
                        res.onSuccess {
                            authUser?.accessToken = it.accessToken
                            physicalLog = it.data
                        }
                    }
                }
            }
        }
    }
}


private interface MiniTouchLogRowProps: Props {
    var name: String?
    var cardId: String
    var time: String
    var accept: Boolean
    var isXsSize: Boolean
}


private val MiniTouchLogRow = FC<MiniTouchLogRowProps> { props ->
    TableRow {
        TableCell {
            if(props.name != null)
                +(props.name)!!
            else
                NavLink {
                    to = "/cards?i=${props.cardId}"
                    +"Add this"
                }
        }
        TableCell {
            +props.time
        }
        TableCell {
            if(props.accept)
                Check { color = SvgIconColor.success }
            else
                Block { color = SvgIconColor.error }
        }
    }
}

private interface MiniTouchLogProps: Props {
    var log: List<CardTouchLog>
    var onClickRefresh: ()->Unit
}

private val MiniTouchLog = FC<MiniTouchLogProps> { props ->

    Card {
        CardHeader {
            sx {
                paddingBottom = 0.px
            }
            title = ReactNode("Touch Log")
        }
        CardContent {
            TableContainer {
                Table {
                    TableHead {
                        TableCell {
                            +"Name"
                        }
                        TableCell {
                            +"Time"
                        }
                        TableCell {
                            +"Accept"
                        }
                    }
                    TableBody {
                        props.log.forEach { log ->
                            MiniTouchLogRow {
                                name = log.name
                                cardId = log.cardId
                                time = toSortDatetimeString(log.time)
                                accept = log.accept
                            }
                        }
                    }
                }
            }
        }
        CardActions {
            Tooltip {
                title = ReactNode("Refresh")
                IconButton {
                    ariaLabel = "refresh"
                    size = Size.medium
                    onClick = { props.onClickRefresh() }
                    +Sync.create()
                }
            }
            ReactHTML.div {
                css {
                    flexGrow = number(1.0)
                }
            }
            NavLink {
                sx {
                    marginRight = 8.px
                }
                to = "/touchlog"
                +"View more"
            }
        }
    }
}


private interface MiniPhysicalLogRowProps: Props {
    var action: String
    var time: String
}


private val MiniPhysicalLogRow = FC<MiniPhysicalLogRowProps> { props ->
    TableRow {
        TableCell {
            +props.action
        }
        TableCell {
            +props.time
        }
    }
}

private interface MiniPhysicalLogProps: Props {
    var log: List<DoorLog>
    var onClickRefresh: ()->Unit
}

private val MiniPhysicalLog = FC<MiniPhysicalLogProps> { props ->
    Card {
        CardHeader {
            sx {
                paddingBottom = 0.px
            }
            title = ReactNode("Physical Log")
        }
        CardContent {
            TableContainer {
                Table {
                    TableHead {
                        TableCell {
                            +"Action"
                        }
                        TableCell {
                            +"Time"
                        }
                    }
                    TableBody {
                        props.log.forEach { log ->
                            MiniPhysicalLogRow {
                                action = log.action
                                time = toSortDatetimeString(log.time)
                            }
                        }
                    }
                }
            }
        }
        CardActions {
            Tooltip {
                title = ReactNode("Refresh")
                IconButton {
                    ariaLabel = "refresh"
                    size = Size.medium
                    onClick = { props.onClickRefresh() }
                    +Sync.create()
                }
            }
            ReactHTML.div {
                css {
                    flexGrow = number(1.0)
                }
            }
            NavLink {
                sx {
                    marginRight = 8.px
                }
                to = "/physicallog"
                +"View more"
            }
        }
    }
}