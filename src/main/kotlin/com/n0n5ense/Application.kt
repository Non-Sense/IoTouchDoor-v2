package com.n0n5ense

import com.n0n5ense.persistence.databaseInit
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.n0n5ense.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = false
        })
    }
    install(Routing)
    configureRouting()
    configureSecurity(environment)
    Database.connect(environment.config.property("database.path").getString(), "org.sqlite.JDBC")
    databaseInit()
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
    ) {
    }.start(wait = true)
}
