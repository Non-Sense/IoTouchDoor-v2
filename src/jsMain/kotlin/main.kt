import kotlinx.browser.document
import kotlinx.browser.window
import react.create
import react.dom.render

val serverAddress = window.location.origin

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    render(Application.create(), container)
}
