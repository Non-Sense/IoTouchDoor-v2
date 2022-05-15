package com.n0n5ense

import com.n0n5ense.door.Door
import com.n0n5ense.door.DoorByGpio
import com.n0n5ense.felica.FelicaHandler
import com.n0n5ense.model.PhysicalLogAction
import com.n0n5ense.persistence.PhysicalLogService
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

private lateinit var door: Door
private lateinit var felicaHandler: FelicaHandler

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
    init(environment)
    environment.monitor.subscribe(ApplicationStopping){
        felicaHandler.onError = null
        felicaHandler.close()
    }
}

private fun init(environment: ApplicationEnvironment){
    Database.connect(environment.config.property("database.path").getString(), "org.sqlite.JDBC")
    databaseInit()

    door = DoorByGpio(environment.config)
    door.onClose = { PhysicalLogService.add(PhysicalLogAction.Close) }
    door.onOpen = { PhysicalLogService.add(PhysicalLogAction.Open) }
    door.onUnlock = { PhysicalLogService.add(PhysicalLogAction.Unlock) }
    door.onLock = { PhysicalLogService.add(PhysicalLogAction.Lock) }

    felicaHandler = FelicaHandler()
    felicaHandler.onAccepted = { door.unlock() }
    felicaHandler.onError = {
        environment.log.error(it.stackTraceToString())
        felicaHandler.init()
    }
    felicaHandler.init()


}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
    ) {
    }.start(wait = true)
}
