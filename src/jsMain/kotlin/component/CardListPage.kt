package component

import mui.material.Container
import react.FC
import react.Props

val CardListPage = FC<Props> {
    Container {
        CardList()
    }
}