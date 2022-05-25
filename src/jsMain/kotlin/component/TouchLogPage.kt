package component

import csstype.*
import mui.material.Container
import mui.system.sx
import react.FC
import react.Props

val TouchLogPage = FC<Props> {
    Container {
//        sx {
//            height = 100.vh - headerHeight
//            overflow = Auto.auto
//        }
        TouchLog()
    }
}