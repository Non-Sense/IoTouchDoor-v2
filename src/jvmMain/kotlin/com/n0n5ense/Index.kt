package com.n0n5ense

import kotlinx.html.*

fun HTML.index(){
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/keylocker2.js") {}
    }
}