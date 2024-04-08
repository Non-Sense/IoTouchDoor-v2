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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.hikali_47041.DiscordBot
import net.hikali_47041.RealDoorBell
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

private fun openFelicaReader(environment: ApplicationEnvironment) {
    CoroutineScope(Dispatchers.Default).launch {
        while(true) {
            val result = FelicaService.open()
            if(result.isSuccess)
                break
            result.onFailure {
                environment.log.error(it.stackTraceToString())
            }
            delay(1000)
        }
        FelicaService.onError = {
            launch {
                delay(1000)
                FelicaService.open()
            }
        }
    }
}

private fun openMagneticReader(path: String) {
    CoroutineScope(Dispatchers.Default).launch {
        MagneticReader.onError = {
            launch {
                delay(1000)
                MagneticReader.open(path)
            }
        }
        while(true) {
            val result = MagneticReader.open(path)
            if(result.isSuccess)
                break
            delay(1000)
        }
    }
}

private fun init(environment: ApplicationEnvironment) {
    Database.connect(environment.config.property("database.path").getString(), "org.sqlite.JDBC")
    databaseInit()
    var doorBell: RealDoorBell? = null

    if(!environment.config.property("gpio.mock").getString().toBoolean()) {
        DoorService.init(DoorByGpio(environment.config, environment))
        DoorService.onActionCallback = {
            runCatching {
                PhysicalLogService.add(it)
                doorBell?.changeBusLedState(false)
            }
        }
    }

    FelicaService.enabled = environment.config.property("feature.felica").getString().toBoolean()
    if(FelicaService.enabled)
        openFelicaReader(environment)

    MagneticReader.enabled = environment.config.property("feature.magnetic").getString().toBoolean()
    if(MagneticReader.enabled)
        openMagneticReader(environment.config.property("feature.magneticReaderPath").getString())

    val bot = runCatching {
        DiscordBot(
            discordBotToken = environment.config.property("notifier.discordToken").getString(),
            channelId = environment.config.property("notifier.channelId").getString(),
            voiceChannelId = environment.config.property("notifier.voiceChannelId").getString(),
            audioPath = environment.config.property("notifier.audioPath").getString(),
            entryAudioPath = environment.config.property("notifier.entryAudioPath").getString(),
            busAudioPath = environment.config.property("notifier.busAudioPath").getString(),
        ).apply {
            start()
        }
    }.getOrNull()

    doorBell = bot?.let {
        RealDoorBell(
            port = environment.config.property("notifier.buttonPort").getString().toInt(),
            busButtonPort = environment.config.property("notifier.busButtonPort").getString().toInt(),
            busLedPort = environment.config.property("notifier.busLedPort").getString().toInt(),
            onBusButtonPushed = {
                runCatching {
                    doorBell?.changeBusLedState(true)
                    bot.sendBusNotify()
                }
            },
            onButtonPushed = {
                runCatching {
                    bot.sendNotify()
                }
            }
        )
    }

    RebootService(
        pin = environment.config.property("gpio.rebootPort").getString().toInt(),
        pushDelay = environment.config.property("gpio.rebootButtonDelayMillis").getString().toLong()
    )

    FelicaService.onTouch = {
        runCatching {
            bot?.sendEntrySound()
        }
    }
    MagneticReader.onTouch = {
        runCatching {
            bot?.sendEntrySound()
        }
    }

    CoroutineScope(Dispatchers.Default).launch {
        while(true) {
            val str = readLine() ?: continue
            when(str) {
                "n" -> doorBell?.pushButton()
                "m" -> bot?.sendEntrySound()
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args)
    ) {
    }.start(wait = true)
}
