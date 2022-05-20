import cookie.Cookies
import mui.material.styles.ThemeOptions
import mui.material.styles.createTheme
import mui.system.useTheme
import react.*
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

class ApplicationState: State {
    var refreshToken: String? = null
}

val ApplicationRoot = FC<Props> {
    +Application().render()
}

class Application : Component<Props, ApplicationState>() {

    init {
        state = ApplicationState().apply {
            refreshToken = Cookies.get("rt")
        }
    }

    private val app = FC<Props> {
        BrowserRouter {
            ThemeModule {

                Routes {
                    Route {
                        path = "/login"
                        element = LoginPage.create {
                            onLogin = {
                                this@Application.state.refreshToken = it.refreshToken.refreshToken
                                Cookies.set("rt", it.refreshToken.refreshToken)
                            }
                        }
                    }

                    Route {
                        index = true
                        element = Welcome.create { name = "kt"}
                    }
                }
            }

        }
    }

    override fun render(): ReactNode {
        return app.create()
    }

}