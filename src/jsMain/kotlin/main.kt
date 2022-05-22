import kotlinx.browser.document
import react.create
import react.dom.render

const val serverAddress = "http://localhost:8080"

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    render(Application.create(), container)
}
