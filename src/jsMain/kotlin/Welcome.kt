import com.n0n5ense.model.json.DoorStatus
import csstype.AtRules
import csstype.px
import csstype.rgb
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mui.material.Container
import mui.material.MuiGrid.Companion.container
import react.FC
import react.Props
import react.css.css
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState

external interface WelcomeProps: Props {
    var name: String
}

val Welcome = FC<WelcomeProps> { props ->
    var name by useState(props.name)
    div {
        css {
            padding = 5.px
            backgroundColor = rgb(8, 97, 22)
            color = rgb(56, 246, 137)
        }
        +"Hello, $name"
    }

    Container {
        input {
            css {
                marginTop = 5.px
                marginBottom = 5.px
                fontSize = 14.px
            }
            type = InputType.text
            value = name
            onChange = { event ->
                name = event.target.value
            }
        }

        button {
            onClick = {
                console.log("clicked!!!!??!!!")
                MainScope().launch {
                    getDoorStatus().let{
                        console.log("received: $it")
                    }
                }
            }
            +"It's button!"
        }
    }


}

private val DoorStatusView = FC<Props> {

}

private suspend fun getDoorStatus(): DoorStatus{
    val response = window
        .fetch("http://localhost:8080/api/testmodel")
        .await()
        .text()
        .await()
    return Json.decodeFromString(response)
}