import cookie.Cookies
import mui.material.CssBaseline
import mui.material.styles.Theme
import mui.material.styles.ThemeProvider
import react.*


typealias ThemeState = StateInstance<Theme>

val ThemeContext = createContext<ThemeState>()

val ThemeModule = FC<PropsWithChildren> { props ->
    val state = useState(if(Cookies.get("dk")?.toBoolean() == true) Themes.Dark else Themes.Light)
    val theme by state

    Cookies.set("dk", (theme == Themes.Dark).toString())

    ThemeContext.Provider(state) {
        ThemeProvider {
            this.theme = theme

            CssBaseline()
            +props.children!!
        }
    }

}