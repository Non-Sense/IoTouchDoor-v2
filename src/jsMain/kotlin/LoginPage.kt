import com.n0n5ense.model.json.LoginUser
import com.n0n5ense.model.json.RefreshToken
import csstype.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mui.material.*
import mui.system.sx
import org.w3c.fetch.RequestInit
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.InputType
import react.dom.onChange

data class LoginInfo(
    val refreshToken: RefreshToken
)

external interface LoginComponentProps: Props {
    var onLogin: ((LoginInfo) -> Unit)
}

private interface InputValue {
    var value: String
}

val LoginPage = FC<LoginComponentProps> { props ->
    var userId = ""
    var password = ""

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
                            userId = (it.target.unsafeCast<InputValue>().value)
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
                            password = (it.target.unsafeCast<InputValue>().value)
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
                                attemptLogin(LoginUser(userId, password)){
                                    it.onSuccess { token ->
                                        props.onLogin(LoginInfo(token))
                                    }.onFailure {
                                        Snackbar {
                                            onClose = { _,_ ->

                                            }
                                            open = true
                                            autoHideDuration = 3000
                                            Alert {
                                                severity = AlertColor.error
                                                +"Login failed"
                                            }
                                            sx {
                                                width = 100.vw
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun attemptLogin(user: LoginUser, callback:((Result<RefreshToken>)->Unit)){
    MainScope().launch {
        callback.invoke(postJsonData("$serverAddress/api/user/auth", user))
    }
}
