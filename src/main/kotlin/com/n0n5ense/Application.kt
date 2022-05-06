package com.n0n5ense

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.n0n5ense.plugins.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module(){
    install(Routing){

    }
    configureRouting()
    configureSecurity(environment)
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
    ) {
    }.start(wait = true)
}
