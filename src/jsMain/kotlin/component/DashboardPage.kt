package component

import AuthUserContext
import CardId
import ThemeContext
import Themes
import com.n0n5ense.model.json.*
import csstype.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mui.icons.material.*
import mui.material.*
import mui.material.Size
import mui.system.ResponsiveStyleValue
import mui.system.Theme
import mui.system.sx
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML
import react.router.dom.NavLink
import util.*
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

private var fetchJob: Job? = null

val DashBoard = FC<Props> {

    val authUser by useContext(AuthUserContext)
    var touchLog by useState<List<CardTouchLog>>(listOf())
    var doorStatus by useState(DoorStatus(false, false, false))
    var readerStatus by useState<List<ReaderDeviceInfo>>(listOf())
    var physicalLog by useState<List<DoorLog>>(listOf())
    var escapeMode by useState(EscapeMode(false))

    fun fetch() {
        MainScope().launch {
            getTouchLog(0, 5, authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                touchLog = it.data
            }
            getPhysicalLog(0, 5, authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                physicalLog = it.data
            }
            getDoorStatus(authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                doorStatus = it.data
            }
            getReaderStatus(authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                readerStatus = it.data
            }
            getEscapeMode(authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                escapeMode = it.data
            }
        }
    }

    useEffectOnce {
        fetch()
        fetchJob?.cancel()
        fetchJob = MainScope().launch {
            while (true) {
                delay(dashboardFetchInterval)
                fetch()
            }
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
            ReaderStatusView {
                this.list = readerStatus
                this.onClickRefresh = {
                    MainScope().launch {
                        getReaderStatus(authUser!!).onSuccess {
                            authUser?.accessToken = it.accessToken
                            readerStatus = it.data
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
            DoorStatusView {
                this.status = doorStatus
                this.escapeMode = escapeMode
                this.onClickRefresh = {
                    MainScope().launch {
                        getDoorStatus(authUser!!).onSuccess {
                            authUser?.accessToken = it.accessToken
                            doorStatus = it.data
                        }
                    }
                }
                this.onCLickDoorAction = { action ->
                    MainScope().launch {
                        postDoorLockStatus(authUser!!, action).onSuccess {
                            authUser?.accessToken = it.accessToken
                        }
                    }
                }
                this.onClickDisableEscapeMode = {
                    MainScope().launch {
                        postEscapeMode(false, authUser!!).onSuccess {
                            authUser?.accessToken = it.accessToken
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
            MiniTouchLog {
                this.log = touchLog
                this.onClickRefresh = {
                    MainScope().launch {
                        getTouchLog(0, 5, authUser!!).onSuccess {
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
                    MainScope().launch {
                        getPhysicalLog(0, 5, authUser!!).onSuccess {
                            authUser?.accessToken = it.accessToken
                            physicalLog = it.data
                        }
                    }
                }
            }
        }
    }
}


private interface MiniTouchLogRowProps : Props {
    var name: String?
    var cardId: CardId
    var time: String
    var accept: Boolean
    var isXsSize: Boolean
}


private val MiniTouchLogRow = FC<MiniTouchLogRowProps> { props ->
    TableRow {
        TableCell {
            sx {
                wordBreak = WordBreak.breakAll
            }
            if (props.name != null)
                +(props.name)!!
            else
                NavLink {
                    to = "/cards?i=${props.cardId.id}&t=${props.cardId.type.name}"
                    +"Add this"
                }
        }
        TableCell {
            +props.time
        }
        TableCell {
            if (props.accept)
                Check { color = SvgIconColor.success }
            else
                Block { color = SvgIconColor.error }
        }
    }
}

private interface MiniTouchLogProps : Props {
    var log: List<CardTouchLog>
    var onClickRefresh: () -> Unit
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


private interface MiniPhysicalLogRowProps : Props {
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

private interface MiniPhysicalLogProps : Props {
    var log: List<DoorLog>
    var onClickRefresh: () -> Unit
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

private interface DoorStatusProps : Props {
    var status: DoorStatus
    var onClickRefresh: () -> Unit
    var onCLickDoorAction: (DoorLockAction) -> Unit
    var escapeMode: EscapeMode
    var onClickDisableEscapeMode: () -> Unit
}

private val DoorStatusView = FC<DoorStatusProps> { props ->
    val theme by useContext(ThemeContext)
    Card {
        sx {
            val isIllegalState = !(props.status.active && props.status.isLock && props.status.isClose)
            backgroundColor = backgroundColorPicker(theme, isIllegalState)
        }
        CardHeader {
            sx {
                paddingBottom = 0.px
            }
            title = ReactNode("Door Status")
        }
        CardContent {
            if (props.escapeMode.enable) {
                Toolbar {
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                    }
                    Typography {
                        variant = "h6"
                        sx {
                            color = Color("#f00000")
                        }
                        +"EscapeMode is enabled"
                    }
                    Button {
                        variant = ButtonVariant.contained
                        onClick = {
                            props.onClickDisableEscapeMode()
                        }
                        +"Disable"
                    }
                }
            }
            if (props.status.active) {
                Toolbar {
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                    }
                    if (props.status.isClose) {
                        DoorFront()
                        Typography {
                            variant = "h6"
                            +"Close"
                        }
                    } else {
                        MeetingRoom()
                        Typography {
                            variant = "h6"
                            +"Open"
                        }
                    }
                }
                Toolbar {
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                    }
                    if (props.status.isLock) {
                        Lock()
                        Typography {
                            variant = "h6"
                            +"Lock"
                        }
                    } else {
                        LockOpen()
                        Typography {
                            variant = "h6"
                            +"Unlock"
                        }
                    }
                }
            } else {
                Toolbar {
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                    }
                    QuestionMark()
                    Typography {
                        variant = "h6"
                        +"Unknown"
                    }
                }
                Toolbar {
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                    }
                    QuestionMark()
                    Typography {
                        variant = "h6"
                        +"Unknown"
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
            Button {
                variant = ButtonVariant.text
                onClick = {
                    props.onCLickDoorAction.invoke(DoorLockAction("lock"))
                }
                +"Lock"
            }
            Button {
                variant = ButtonVariant.text
                onClick = {
                    props.onCLickDoorAction.invoke(DoorLockAction("unlock"))
                }
                +"Unlock"
            }
        }
    }
}

private interface ReaderStatusViewProps : Props {
    var list: List<ReaderDeviceInfo>
    var onClickRefresh: () -> Unit
}

private val ReaderStatusView = FC<ReaderStatusViewProps> { props ->
    Card {
        val theme by useContext(ThemeContext)
        sx {
            val isIllegalState = props.list.find { !it.connected } != null || props.list.isEmpty()
            backgroundColor = backgroundColorPicker(theme, isIllegalState)
        }
        CardHeader {
            sx {
                paddingBottom = 0.px
            }
            title = ReactNode("Reader Status")
        }
        CardContent {
            if (props.list.isEmpty()) {
                +"Not Configured"
            } else {
                Table {
                    TableHead {
                        TableCell {
                            +"Type"
                        }
                        TableCell {
                            +"Status"
                        }
                    }
                    TableBody {
                        props.list.forEach { readerInfo ->
                            TableRow {
                                TableCell {
                                    +readerInfo.type
                                }
                                TableCell {
                                    sx {
                                        wordBreak = WordBreak.breakAll
                                    }
                                    +if (readerInfo.connected) readerInfo.name else "Not Connected"
                                }
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
        }
    }
}

private fun backgroundColorPicker(theme: Theme, isIllegalState: Boolean): Color {
    return when {
        theme == Themes.Light && !isIllegalState -> "#ddf2f2"
        theme == Themes.Dark && !isIllegalState -> "#003232"
        theme == Themes.Light && isIllegalState -> "#f2f2dd"
        theme == Themes.Dark && isIllegalState -> "#191900"
        else -> "#f2f2dd"
    }.let { Color(it) }
}