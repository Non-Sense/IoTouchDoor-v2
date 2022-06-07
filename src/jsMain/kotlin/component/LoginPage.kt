package component

import AuthUser
import AuthUserContext
import com.n0n5ense.model.json.LoginUser
import com.n0n5ense.model.json.RefreshToken
import cookie.Cookies
import csstype.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.material.*
import mui.system.sx
import util.postJsonData
import react.*
import react.dom.html.InputType
import react.dom.onChange
import react.router.useNavigate
import serverAddress
import tokenCookieName
import util.value

val LoginPage = FC<Props> {
    var userId by useState("")
    var password by useState("")
    var snackbarOpen by useState(false)

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var authUser by useContext(AuthUserContext)
    val navigate = useNavigate()

    fun login() {
        attemptLogin(LoginUser(userId, password)) {
            it.onSuccess { token ->
                Cookies.set(tokenCookieName, token.refreshToken)
                authUser = AuthUser(token.refreshToken, null)
                navigate("/dashboard")
            }.onFailure {
                snackbarOpen = true
            }
        }
    }

    Grid {
        container = true
        sx {
            marginTop = 30.px
            alignItems = AlignItems.center
            justifyItems = JustifyItems.center
            justifyContent = JustifyContent.center
        }
        Grid {
            xs = 8
            Card {
                sx {
                    alignItems = AlignItems.center
                }

                CardContent {
                    sx {
                        display = Display.grid
                    }
                    Typography {
                        variant = "h5"
                        sx {
                            paddingBottom = 30.px
                        }
                        +"Login"
                    }
                    TextField {
                        id = "userid"
                        label = ReactNode("User ID")
                        variant = FormControlVariant.outlined

                        onChange = {
                            userId = (it.target.value)
                        }
                        onKeyDown = {
                            if(it.key == "Enter")
                                login()
                        }
                    }
                    TextField {
                        id = "password"
                        label = ReactNode("Password")
                        variant = FormControlVariant.outlined
                        type = InputType.password
                        sx {
                            marginTop = 10.px
                        }
                        onChange = {
                            password = (it.target.value)
                        }
                        onKeyDown = {
                            if(it.key == "Enter")
                                login()
                        }
                    }
                    CardActions {
                        sx {
                            marginTop = 30.px
                        }
                        Button {
                            +"Login"
                            variant = ButtonVariant.contained
                            onClick = {
                                login()
                            }
                        }
                    }
                }
            }
        }
    }
    Snackbar {
        onClose = { _, _ ->
            snackbarOpen = false
        }
        open = snackbarOpen
        autoHideDuration = 2000
        Alert {
            severity = AlertColor.error
            +"Login failed"
        }
        sx {
            width = 100.vw
        }
    }
}

private fun attemptLogin(user: LoginUser, callback: ((Result<RefreshToken>) -> Unit)) {
    MainScope().launch {
        callback.invoke(postJsonData("$serverAddress/api/user/auth", user))
    }
}
