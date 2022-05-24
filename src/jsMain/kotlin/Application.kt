import component.LoginPage
import component.TouchLog
import cookie.Cookies
import react.*
import react.router.Navigate
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

const val tokenCookieName = "rt"

val Application = FC<Props> {
    BrowserRouter {
        ThemeModule {

            AuthUserContext.Provider {
                this.value = useState(Cookies.get(tokenCookieName)?.let { AuthUser(it, null) })
                Routes {
                    Route {
                        path = "/login"
                        element = LoginPage.create()
                    }

                    Route {
                        path = "/logout"
                        element = FC<Props> {
                            Cookies.remove(tokenCookieName)
                            Navigate {
                                to = "/login"
                            }
                        }.create()
                    }

                    Route {
                        index = true
                        element = Welcome.create { name = "kt"}
                    }

                    Route {
                        path = "/unk"
                        element = PrivateElement.create {
                            Welcome { name = "prii2"}
                        }
                    }

                    Route {
                        path = "/touchlog"
                        element = PrivateElement.create {
                            TouchLog { }
                        }
                    }
                }
            }
        }
    }
}