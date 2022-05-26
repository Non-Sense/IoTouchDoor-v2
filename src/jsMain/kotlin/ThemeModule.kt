import mui.material.CssBaseline
import mui.material.styles.Theme
import mui.material.styles.ThemeProvider
import react.*


typealias ThemeState = StateInstance<Theme>

val ThemeContext = createContext<ThemeState>()

val ThemeModule = FC<PropsWithChildren> { props ->
    val state = useState(Themes.Light)
    val theme by state

    ThemeContext.Provider(state) {
        ThemeProvider {
            this.theme = theme

            CssBaseline()
            +props.children!!
        }
    }

}