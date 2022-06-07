import component.*
import cookie.Cookies
import react.*
import react.router.Navigate
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import react.router.useNavigate

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

                    Route {
                        path = "/cards"
                        element = PrivateElement.create {
                            WithHeader {
                                name = "Card list"
                                CardListPage()
                            }
                        }
                    }

                    Route {
                        path = "/dashboard"
                        element = PrivateElement.create {
                            WithHeader {
                                name = "Dashboard"
                                DashboardPage()
                            }
                        }
                    }

                    Route {
                        index = true
                        path = "/"
                        element = FC<Props> {
                            Navigate {
                                to = "/dashboard"
                            }
                        }.create()
                    }
                }
            }
        }
    }
}