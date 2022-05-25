import kotlinx.js.jso
import mui.material.PaletteMode
import mui.material.styles.createTheme

object Themes {
    val Light = createTheme(
        jso {
            palette = jso {
                mode = PaletteMode.light
                primary = jso {
                    main = "#16b5b5"
                }
                secondary = jso {
                    main = "#e12885"
                }
            }
        }
    )

    val Dark = createTheme(
        jso {
            palette = jso {
                mode = PaletteMode.dark
                primary = jso {
                    main = "#16b5b5"
                }
                secondary = jso {
                    main = "#e12885"
                }
            }
        }
    )
}