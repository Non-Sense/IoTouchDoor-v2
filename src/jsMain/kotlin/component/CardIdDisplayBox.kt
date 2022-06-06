package component

import CardId
import csstype.AlignItems
import csstype.Display
import csstype.px
import csstype.rem
import mui.material.Box
import mui.system.sx
import react.FC
import react.Props
import react.css.css
import react.dom.html.ReactHTML

interface CardIdProps : Props {
    var cardId: CardId
}

val CardIdDisplayBox = FC<CardIdProps> { props ->
    Box {
        sx {
            display = Display.flex
            alignItems = AlignItems.center
        }
        if (props.cardId.type == CardIdType.GunmaUniv) {
            ReactHTML.img {
                src = "static/gunma_logo.png"
                css {
                    height = 1.rem
                    width = 1.rem
                    marginRight = 3.px
                }
            }
        }
        +props.cardId.id
    }
}