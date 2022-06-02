package com.n0n5ense

import com.n0n5ense.door.DoorByGpio
import com.n0n5ense.door.DoorService
import com.n0n5ense.felica.FelicaService
import com.n0n5ense.magnetic.MagneticReader
import com.n0n5ense.persistence.PhysicalLogService
import com.n0n5ense.persistence.databaseInit
import com.n0n5ense.plugins.configureRouting
import com.n0n5ense.plugins.configureSecurity
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
    init(environment)
    environment.monitor.subscribe(ApplicationStopping) {
        FelicaService.onError = null
        FelicaService.close()
        MagneticReader.onError = null
        MagneticReader.close()
    }
}

private fun init(environment: ApplicationEnvironment) {
    Database.connect(environment.config.property("database.path").getString(), "org.sqlite.JDBC")
    databaseInit()

    DoorService.init(DoorByGpio(environment.config))
    DoorService.onActionCallback = { PhysicalLogService.add(it) }

    if (environment.config.property("feature.felica").getString().toBoolean()) {
        FelicaService.enabled = true
        FelicaService.onError = {
            environment.log.error(it.stackTraceToString())
            FelicaService.open()
        }
        FelicaService.open()
    } else {
        FelicaService.enabled = false
    }

    if (environment.config.property("feature.magnetic").getString().toBoolean()) {
        MagneticReader.enabled = true
        MagneticReader.onError = {
            environment.log.error(it.stackTraceToString())
            MagneticReader.open(environment.config.property("feature.magneticReaderPath").getString())
        }
        MagneticReader.open(environment.config.property("feature.magneticReaderPath").getString())
    } else {
        MagneticReader.enabled = false
    }

}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
    ) {
    }.start(wait = true)
}
