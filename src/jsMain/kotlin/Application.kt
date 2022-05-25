import component.*
import cookie.Cookies
import react.FC
import react.Props
import react.create
import react.router.Navigate
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import react.useState

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
                        element = Welcome.create { name = "kt" }
                    }

                    Route {
                        path = "/unk"
                        element = PrivateElement.create {
                            Welcome { name = "prii2" }
                        }
                    }

                    Route {
                        path = "/touchlog"
                        element = PrivateElement.create {
                            WithHeader {
                                name = "Touch log"
                                TouchLogPage()
                            }
                        }
                    }

                    Route {
                        path = "/physicallog"
                        element = PrivateElement.create {
                            WithHeader {
                                name = "Physical log"
                                PhysicalLogPage()
                            }
                        }
                    }
                }
            }
        }
    }
}