package component

import AuthUserContext
import util.addCard
import com.n0n5ense.model.json.EditTouchCard
import com.n0n5ense.model.json.NewTouchCard
import com.n0n5ense.model.json.TouchCard
import csstype.number
import csstype.px
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import util.deleteCard
import util.getCardCount
import util.getCards
import mui.icons.material.Add
import mui.icons.material.Block
import mui.icons.material.Check
import mui.icons.material.Edit
import mui.material.*
import mui.system.Breakpoint
import mui.system.ResponsiveStyleValue
import mui.system.Theme
import mui.system.sx
import org.w3c.dom.url.URLSearchParams
import util.putCard
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.onChange
import react.router.useLocation
import util.value

private interface CardListRowProps : Props {
    var card: TouchCard
    var onClickEditButton: () -> Unit
    var isXsSize: Boolean
}

private val CardListRow = FC<CardListRowProps> { props ->
    TableRow {
        TableCell {
            +props.card.name
        }
        if (!props.isXsSize)
            TableCell {
                +props.card.cardId
            }
        TableCell {
            if (props.card.enabled)
                Check { color = SvgIconColor.success }
            else
                Block { color = SvgIconColor.error }
        }
        TableCell {
            IconButton {
                onClick = {
                    props.onClickEditButton.invoke()
                }
                Edit()
            }
        }
    }
}

val CardList = FC<Props> {
    val search = useLocation().search
    var cards by useState<List<TouchCard>>(listOf())
    val authUser by useContext(AuthUserContext)
    var page by useState(0)
    var width by useState(50)
    var count by useState(0L)

    var dialogOpen by useState(false)
    var alertDialogOpen by useState(false)
    var dialogCardInfo by useState<TouchCard?>(null)

    var editCardId by useState("")
    var editCardIdDisable by useState(true)
    var editCardName by useState("")
    var editEnabled by useState(false)
    var editIsAddMode by useState(false)

    val isXsSize = useMediaQuery<Theme>(queryInput = {
        it.breakpoints.down(Breakpoint.sm)
    })

    fun updateCards(p: Int? = null, w: Int? = null) {
        MainScope().launch {
            getCards(p ?: page, w ?: width, authUser!!).onSuccess {
                cards = it.data
                authUser?.accessToken = it.accessToken
            }
        }

    }

    fun updateCount(callback: ((Result<Long>) -> Unit)? = null) {
        MainScope().launch {
            getCardCount(authUser!!)
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

    fun onDialogClose(card: TouchCard?) {
        if (card == null || authUser == null) {
            dialogOpen = false
            alertDialogOpen = false
            return
        }
        if (editIsAddMode) {
            MainScope().launch {
                addCard(
                    NewTouchCard(card.name, card.cardId, card.enabled, card.owner),
                    authUser!!
                ).onSuccess {
                    authUser?.accessToken = it.accessToken
                    dialogOpen = false
                    alertDialogOpen = false
                    updateCount {
                        updateCards()
                    }
                }
            }

        } else {
            MainScope().launch {
                putCard(
                    card.id,
                    EditTouchCard(card.name, card.enabled),
                    authUser!!
                ).onSuccess {
                    authUser?.accessToken = it.accessToken
                    dialogOpen = false
                    alertDialogOpen = false
                    updateCount {
                        updateCards()
                    }
                }
            }
        }
    }

    fun onClickDelete(card: TouchCard?) {
        if (card == null || authUser == null) {
            dialogOpen = false
            alertDialogOpen = false
            return
        }
        MainScope().launch {
            deleteCard(card.id, authUser!!).onSuccess {
                authUser?.accessToken = it.accessToken
                dialogOpen = false
                alertDialogOpen = false
                updateCount {
                    updateCards()
                }
            }
        }
    }

    useEffectOnce {
        updateCount {
            updateCards()
        }
        val query = URLSearchParams(search)
        query.get("i")?.let {
            editCardId = it
            editCardName = ""
            editCardIdDisable = true
            editEnabled = true
            editIsAddMode = true
            dialogOpen = true
        }
    }

    Toolbar {
        css {
            flexGrow = number(1.0)
        }
        div {
            css {
                flexGrow = number(1.0)
            }
        }
        Fab {
            sx {
                paddingLeft = 5.px
            }
            variant = FabVariant.extended
            color = "secondary".unsafeCast<FabColor>()
            Add()
            +ReactNode("Add")
            size = Size.medium
            onClick = {
                dialogCardInfo = null
                editCardId = ""
                editCardName = ""
                editEnabled = true
                editCardIdDisable = false
                editIsAddMode = true
                dialogOpen = true
            }
        }
    }

    TableContainer {
        Table {
            TableHead {
                TableCell {
                    +"Name"
                }
                if (!isXsSize)
                    TableCell {
                        +"Card ID"
                    }
                TableCell {
                    +"Enable"
                }
                TableCell {
                }
            }
            TableBody {
                cards.forEach { card ->
                    CardListRow {
                        this.card = card
                        this.onClickEditButton = {
                            dialogCardInfo = card
                            editCardId = card.cardId
                            editCardName = card.name
                            editEnabled = card.enabled
                            editCardIdDisable = true
                            editIsAddMode = false
                            dialogOpen = true
                        }
                        this.isXsSize = isXsSize
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
                            updateCards(p = pi)
                        }
                        onRowsPerPageChange = {
                            val wi = it.target.value.unsafeCast<Int>()
                            width = wi
                            updateCards(w = wi)
                        }
                    }
                }
            }
        }
    }

    fun getCardInfo(): TouchCard? {
        if (editIsAddMode) {
            return TouchCard(
                -1,
                editCardName,
                editCardId,
                editEnabled,
                null
            )
        }
        dialogCardInfo ?: return null
        return TouchCard(
            dialogCardInfo!!.id,
            editCardName,
            editCardId,
            editEnabled,
            dialogCardInfo!!.owner
        )
    }

    Dialog {
        open = dialogOpen
        onClose = { _, _ ->
            onDialogClose(null)
        }
        DialogTitle {
            +if (editIsAddMode) "Add info" else "Edit info"
        }

        DialogContent {

            Stack {
                direction = ResponsiveStyleValue(StackDirection.column)
                spacing = ResponsiveStyleValue(3)
                fullWidth = true
                TextField {
                    id = "cardId"
                    disabled = editCardIdDisable
                    label = ReactNode("Card ID")
                    variant = FormControlVariant.standard
                    value = editCardId
                    type = InputType.text
                    fullWidth = true
                    onChange = {
                        editCardId = it.target.value
                    }
                }

                TextField {
                    id = "cardName"
                    label = ReactNode("Card Name")
                    variant = FormControlVariant.standard
                    value = editCardName
                    type = InputType.text
                    fullWidth = true
                    onChange = {
                        editCardName = it.target.value
                    }
                }

                FormControlLabel {
                    control = Switch.create {
                        checked = editEnabled
                        ariaLabel = "Enable"
                        onChange = { _, checked ->
                            editEnabled = checked
                        }
                    }
                    label = ReactNode("Enable")
                }
            }
        }

        DialogActions {
            Button {
                onClick = {
                    onDialogClose(null)
                }
                +"Cancel"
            }
            div {
                css {
                    flexGrow = number(1.0)
                }
            }
            if (!editIsAddMode)
                Button {
                    onClick = {
                        alertDialogOpen = true
                    }
                    color = "error".unsafeCast<ButtonColor>()
                    variant = ButtonVariant.contained
                    +"Delete"
                }
            Button {
                onClick = {
                    onDialogClose(getCardInfo())
                }
                variant = ButtonVariant.contained
                +if (editIsAddMode) "Add" else "Apply"
            }
        }
    }

    Dialog {
        open = alertDialogOpen
        DialogTitle {
            +"Confirm deletion"
        }
        DialogContent {
            Typography {
                +"Are you sure you want to delete this card?"
            }
        }

        DialogActions {
            Button {
                onClick = {
                    onClickDelete(dialogCardInfo)
                }
                color = "error".unsafeCast<ButtonColor>()
                variant = ButtonVariant.contained
                +"Delete"
            }
            Button {
                onClick = {
                    alertDialogOpen = false
                }
                +"Cancel"
            }
        }

    }

}
