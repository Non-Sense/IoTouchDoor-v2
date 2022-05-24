package component

import ThemeContext
import mui.icons.material.Brightness4
import mui.icons.material.Brightness7
import mui.material.*
import react.*
import react.dom.aria.ariaLabel


interface HeaderName: Props {
    var name: String
}

val Header = FC<HeaderName> {
    var theme by useContext(ThemeContext)

    AppBar {
        position = AppBarPosition.fixed

        Toolbar {
            Typography {
                variant = "h6"
                +it.name
            }
        }

        Tooltip {
            title = ReactNode("Theme")

            Switch {
                icon = Brightness7.create()
                checkedIcon = Brightness4.create()
                checked = theme == Themes.Dark
                ariaLabel = "theme"

                onChange = { _, checked ->
                    theme = if (checked) Themes.Dark else Themes.Light
                }
            }
        }


    }

}