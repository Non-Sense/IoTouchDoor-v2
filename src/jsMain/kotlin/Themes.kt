import kotlinx.js.jso
import mui.material.PaletteMode
import mui.material.styles.createTheme

object Themes {
    val Light = createTheme(
        jso {
            palette = jso {
                mode = PaletteMode.light
                primary = jso {
                    main = "#18C8C8"
                }
                secondary = jso {
                    main = "#E12885"
                }
            }
        }
    )

    val Dark = createTheme(
        jso {
            palette = jso {
                mode = PaletteMode.dark
                primary = jso {
                    main = "#18C2C2"
                }
                secondary = jso {
                    main = "#E12885"
                }
            }
        }
    )
}