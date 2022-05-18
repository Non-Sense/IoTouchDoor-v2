@file:JsModule("react-constraint-layout")
@file:JsNonModule

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.HTMLAttributes

@JsName("ConstraintLayout")
external val ConstraintLayout : ComponentClass<ConstraintLayoutProps>

abstract external class ConstraintLayoutProps: Props {
//    var id: String
    var width: String
    var height: String
    var marginTop: String
    var marginLeft: String
    var marginRight: String
    var marginBottom: String
    var leftToLeftOf: Array<String>
    var leftToRightOf: Array<String>
    var rightToRightOf: Array<String>
    var rightToLeftOf: Array<String>
    var topToTopOf: Array<String>
    var bottomToBottomOf: Array<String>
    var bottomToTopOf: Array<String>
    var horizontalBias: Number
    var verticalBias: Number
}