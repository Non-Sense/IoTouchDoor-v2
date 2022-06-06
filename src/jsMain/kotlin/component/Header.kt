package component

import ThemeContext
import Themes
import csstype.*
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.Breakpoint
import mui.system.Theme
import mui.system.sx
import react.*
import react.css.css
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML
import react.router.dom.NavLink
import react.router.useLocation
import react.router.useNavigate

val sideBarWidth = 180.px
val headerHeight = 64.px

data class PageInfo(
    val path: String,
    val name: String,
    val icon: SvgIconComponent
)

fun usePages(): List<PageInfo> {
    return useMemo {
        listOf(
            PageInfo("/dashboard", "Dashboard", Dashboard),
            PageInfo("/touchlog", "Touch log", Dvr),
            PageInfo("/physicallog", "Physical log", FitnessCenter),
            PageInfo("/cards", "Cards", CreditCard)
        )
    }
}

interface HeaderName: Props {
    var name: String
    var isMobileSize: Boolean
}

interface HeaderNameWithChildren: PropsWithChildren, HeaderName


val Header = FC<HeaderName> {
    var theme by useContext(ThemeContext)
    val navigate = useNavigate()
    val isMobileSize = it.isMobileSize

    var isOpen by useState(false)
    val pages = usePages()
    val lastPathname = useLocation().pathname


    AppBar {
        sx {
            gridArea = ident("header")
            zIndex = integer(1500)
        }

        position = AppBarPosition.fixed

        Toolbar {

            if(isMobileSize)
                IconButton {
                    sx {
                        border = Border(1.px, csstype.LineStyle.solid, theme.palette.primary.dark)
                        borderRadius = 10.px
                    }
                    onClick = {
                        isOpen = !isOpen
                    }
                    mui.icons.material.Menu {
                        color = "text".unsafeCast<SvgIconColor>()
                    }
                }

            Typography {
                sx {
                    flexGrow = number(1.0)
                    marginLeft = 8.px
                }
                variant = "h6"
                +it.name
            }

            Tooltip {
                sx {
                    marginRight = 10.px
                }
                title = ReactNode("Theme")

                Switch {
                    icon = Brightness7.create()
                    checkedIcon = Brightness4.create()
                    checked = theme == Themes.Dark
                    ariaLabel = "theme"

                    onChange = { _, checked ->
                        theme = if(checked) Themes.Dark else Themes.Light
                    }
                }
            }

            Tooltip {
                title = ReactNode("Logout")
                IconButton {
                    onClick = {
                        navigate("/logout")
                    }
                    Logout {
                        color = "text".unsafeCast<SvgIconColor>()
                    }
                }

            }
        }
    }

    if(isMobileSize) {
        Box {
            component = ReactHTML.nav

            SwipeableDrawer {
                anchor = DrawerAnchor.left
                open = isOpen
                onOpen = { isOpen = true }
                onClose = { isOpen = false }

                Box {
                    Toolbar()
                    List {
                        sx {
                            width = sideBarWidth
                        }
                        for(page in pages) {
                            NavLink {
                                css {
                                    textDecoration = None.none
                                    color = Color.currentcolor
                                }
                                to = page.path
                                ListItemButton {
                                    selected = lastPathname == page.path
                                    ListItemIcon {
                                        page.icon()
                                    }
                                    ListItemText {
                                        primary = ReactNode(page.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box {
            component = ReactHTML.nav
            sx {
                gridArea = ident("sidebar")
            }

            Drawer {
                variant = DrawerVariant.permanent
                anchor = DrawerAnchor.left

                Box {
                    Toolbar()

                    List {
                        sx { width = sideBarWidth }

                        for(page in pages) {
                            NavLink {
                                to = page.path

                                css {
                                    textDecoration = None.none
                                    color = Color.currentcolor
                                }

                                ListItemButton {
                                    selected = lastPathname == page.path
                                    ListItemIcon {
                                        page.icon()
                                    }
                                    ListItemText {
                                        primary = ReactNode(page.name)
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

val WithHeader = FC<HeaderNameWithChildren> {
    val isMobileSize = useMediaQuery<Theme>(queryInput = { theme ->
        theme.breakpoints.down(Breakpoint.md)
    })
    Box {
        sx {
            display = Display.grid
            gridTemplateRows = array(headerHeight, Auto.auto)
            gridTemplateColumns = array(sideBarWidth, Auto.auto)
            gridTemplateAreas = GridTemplateAreas(
                arrayOf(ident("header"), ident("header")),
                if(isMobileSize)
                    arrayOf(ident("content"), ident("content"))
                else
                    arrayOf(ident("sidebar"), ident("content"))
            )
        }
        Header {
            name = it.name
            this.isMobileSize = isMobileSize
        }
        Box {
            sx {
                gridArea = ident("content")
            }
            +it.children!!
        }
    }


}