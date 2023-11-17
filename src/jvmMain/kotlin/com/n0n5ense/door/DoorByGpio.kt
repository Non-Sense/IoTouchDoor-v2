package com.n0n5ense.door

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.*
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

class DoorByGpio(config: ApplicationConfig, private val environment: ApplicationEnvironment): Door(environment) {

    companion object {
        private const val EscapeModeForceExitTime = 5000
        private const val ForceUnlockTime = 2200
    }

    private val logger = LoggerFactory.getLogger("DoorByGpio")

    private val servoPort = config.property("gpio.servoPort").getString().toInt()
    private val servoSensorPort = config.property("gpio.servoSenorPort").getString().toInt()
    private val doorSensorPort = config.property("gpio.doorSensorPort").getString().toInt()
    private val unlockSwitchPort = config.property("gpio.unlockSwitchPort").getString().toInt()
    private val doorOpenIndicatorPort = config.property("gpio.doorOpenIndicatorPort").getString().toInt()
    private val doorLockIndicatorPort = config.property("gpio.doorLockIndicatorPort").getString().toInt()

    private val servoLockPosition = config.property("gpio.servoLockPosition").getString().toFloat()
    private val servoUnlockPosition = config.property("gpio.servoUnlockPosition").getString().toFloat()
    private val servoWaitTime = config.property("gpio.servoMoveWaitTimeMilli").getString().toInt()

    private val pi4jContext = Pi4J.newAutoContext()
    private val pwm: Pwm = pi4jContext.create(
        Pwm.newConfigBuilder(pi4jContext)
            .id("BCM$servoPort")
            .name("servo")
            .address(servoPort)
            .pwmType(PwmType.SOFTWARE)
            .initial(0)
            .shutdown(0)
            .frequency(50)
            .build()
    )

    private val openIndicator = pi4jContext.create(
        DigitalOutputConfigBuilder.newInstance(pi4jContext)
            .address(doorOpenIndicatorPort)
    ).apply { config().shutdownState(DigitalState.LOW) }

    private val lockIndicator = pi4jContext.create(
        DigitalOutputConfigBuilder.newInstance(pi4jContext)
            .address(doorLockIndicatorPort)
    ).apply { config().shutdownState(DigitalState.LOW) }

    private val servoSensor = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(servoSensorPort)
    )

    private val doorSensor = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(doorSensorPort)
    )

    private val unlockSwitch = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(unlockSwitchPort)
    )

    private val onDoorLockStateChange = DigitalStateChangeListener { event ->
        when(event?.state()) {
            DigitalState.LOW -> lockIndicator.state(DigitalState.HIGH)
            DigitalState.HIGH -> lockIndicator.state(DigitalState.LOW)
            else -> {}
        }
    }

    private val onDoorOpenStateChange = DigitalStateChangeListener { event ->
        when(event?.state()) {
            DigitalState.LOW -> openIndicator.state(DigitalState.HIGH)
            DigitalState.HIGH -> openIndicator.state(DigitalState.LOW)
            else -> {}
        }
    }

    private var lastUnlockSwitchPush = System.currentTimeMillis()
    private val unlockSwitchListener = DigitalStateChangeListener { event ->
        when (event?.state()) {
            DigitalState.LOW -> {
                if (!isEscapeMode)
                    unlock()
                lastUnlockSwitchPush = System.currentTimeMillis()
            }
            DigitalState.HIGH -> {
                val pushDuration = System.currentTimeMillis() - lastUnlockSwitchPush
                if (pushDuration >= EscapeModeForceExitTime) {
                    setEscapeMode(false)
                }
                if (pushDuration >= ForceUnlockTime) {
                    unlock()
                }
            }
            else -> {}
        }
    }


    init {
        servoSensor.addListener(onDoorLockStateChange)
        doorSensor.addListener(onDoorOpenStateChange)
        unlockSwitch.addListener(unlockSwitchListener)
    }

    private var job: Job? = null
    private var isEscapeMode = false

    private var brinkJob: Job? = null

    private fun startBrinkLED() {
        stopBrinkLED()
        brinkJob = CoroutineScope(Dispatchers.Default).launch {
            while(isActive) {
                if(brinkJob?.isCancelled == true)
                    break
                lockIndicator.state(DigitalState.HIGH)
                delay(200)
                lockIndicator.state(DigitalState.LOW)
                delay(800)
            }
            lockIndicator.state(servoSensor.state())
        }
    }

    private fun stopBrinkLED() {
        brinkJob?.cancel()
        brinkJob = null
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }

    private fun moveServo(position: Number, offEnable: Boolean): Job =
        CoroutineScope(Dispatchers.Default).launch {
            logger.info("servo $position")
            pwm.on(position)
            if(!offEnable)
                return@launch
            delay(servoWaitTime.milliseconds)
            logger.info("servo off")
            pwm.on(0)
//            pwm.off()
        }

    override fun unlock() {
        cancelJob()
        job = moveServo(servoUnlockPosition, true)
    }

    override fun lock(force: Boolean) {
        if(getStatus()?.isClose == true || force) {
            cancelJob()
            job = moveServo(servoLockPosition, !isEscapeMode)
        }
    }

    override fun setEscapeMode(enable: Boolean) {
        isEscapeMode = enable
        lock()
        if(enable) {
            startBrinkLED()
        } else {
            stopBrinkLED()
        }
    }

    override fun getEscapeMode(): Boolean {
        return isEscapeMode
    }

    override fun getStatus(): DoorStatus? {
        val isLock = servoSensor.state() ?: return null
        val isClose = doorSensor.state() ?: return null
        if(isLock == DigitalState.UNKNOWN || isClose == DigitalState.UNKNOWN)
            return null
        return DoorStatus(
            isLock.isHigh,
            isClose.isHigh,
        )
    }
}