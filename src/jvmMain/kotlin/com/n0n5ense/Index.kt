package com.n0n5ense

import kotlinx.html.*

fun HTML.index(){
    head {
        title("KeyLocker2")
        meta {
            charset = "UTF-8"
        }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        link(href = "./static/index.css", rel = "stylesheet")
//        link(href = "./static/css/bootstrap.min.css", rel = "stylesheet")
    }
    body {
        div {
            id = "root"
        }
//        script(src = "/static/js/popper.min.js") {}
//        script(src = "/static/js/bootstrap.min.js") {}
        script(src = "/static/keylocker2.js") {}
    }
}