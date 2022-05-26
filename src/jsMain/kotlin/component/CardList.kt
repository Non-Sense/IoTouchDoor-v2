package component

import AuthUser
import AuthUserContext
import DataWithAccessToken
import com.n0n5ense.model.json.Count
import com.n0n5ense.model.json.EditTouchCard
import com.n0n5ense.model.json.NewTouchCard
import com.n0n5ense.model.json.TouchCard
import csstype.number
import csstype.px
import deleteWithTokenRetry
import getJsonDataWithTokenRetry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.Add
import mui.icons.material.Block
import mui.icons.material.Check
import mui.icons.material.Edit
import mui.material.*
import mui.system.Breakpoint
import mui.system.ResponsiveStyleValue
import mui.system.Theme
import mui.system.sx
import postJsonDataWithTokenRetry
import putJsonDataWithTokenRetry
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.onChange
import serverAddress
import value

private interface CardListRowProps: Props {
    var card: TouchCard
    var onClickEditButton: () -> Unit
    var isXsSize: Boolean
}

private val CardListRow = FC<CardListRowProps> { props ->
    TableRow {
        TableCell {
            +props.card.name
        }
        if(!props.isXsSize)
            TableCell {
                +props.card.cardId
            }
        TableCell {
            if(props.card.enabled)
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
        getCards(p ?: page, w ?: width, authUser!!) { result ->
            result.onSuccess {
                cards = it.data
                authUser?.accessToken = it.accessToken
            }
        }
    }

    fun updateCount(callback: ((Result<Long>) -> Unit)? = null) {
        getCardCount(authUser!!) { result ->
            result.onSuccess {
                count = it.data.count
                authUser?.accessToken = it.accessToken
                callback?.invoke(Result.success(it.data.count))
            }.onFailure {
                callback?.invoke(Result.failure(it))
            }
        }
    }

    fun onDialogClose(card: TouchCard?) {
        if(card == null || authUser == null) {
            dialogOpen = false
            alertDialogOpen = false
            return
        }
        if(editIsAddMode) {
            addCard(
                NewTouchCard(card.name, card.cardId, card.enabled, card.owner),
                authUser!!
            ) {
                dialogOpen = false
                alertDialogOpen = false
                updateCount {
                    updateCards()
                }
            }
        } else {
            putCard(
                card.id,
                EditTouchCard(card.name, card.enabled),
                authUser!!
            ) {
                dialogOpen = false
                alertDialogOpen = false
                updateCount {
                    updateCards()
                }
            }
        }
    }

    fun onClickDelete(card: TouchCard?) {
        if(card == null || authUser == null) {
            dialogOpen = false
            alertDialogOpen = false
            return
        }
        deleteCard(card.id, authUser!!){
            dialogOpen = false
            alertDialogOpen = false
            updateCount {
                updateCards()
            }
        }
    }

    useEffectOnce {
        updateCount {
            updateCards()
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
                if(!isXsSize)
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
        if(editIsAddMode){
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
            +if(editIsAddMode) "Add info" else "Edit info"
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
            if(!editIsAddMode)
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
                +if(editIsAddMode) "Add" else "Apply"
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

private fun getCards(
    page: Int,
    width: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<List<TouchCard>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card?p=$page&w=$width", authUser))
    }
}

private fun getCardCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/count", authUser))
    }
}

private fun addCard(
    card: NewTouchCard,
    authUser: AuthUser,
    callback: (Result<DataWithAccessToken<Unit>>) -> Unit
){
    MainScope().launch {
        callback.invoke(postJsonDataWithTokenRetry("$serverAddress/api/card", authUser, card))
    }
}

private fun putCard(
    cardId: Int,
    card: EditTouchCard,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<Unit>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(putJsonDataWithTokenRetry("$serverAddress/api/card/$cardId", authUser, card))
    }
}

private fun deleteCard(
    cardId: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<Unit>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(deleteWithTokenRetry("$serverAddress/api/card/$cardId", authUser))
    }
}