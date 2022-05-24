package component

import react.FC
import react.Props

val TouchLogPage = FC<Props> {
    Header {
        name = "Touch log"
    }
    TouchLog()
}