package com.n0n5ense.door

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInputProvider
import com.pi4j.io.gpio.digital.DigitalOutputProvider
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import io.ktor.server.config.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

class DoorByGpio(config: ApplicationConfig): Door() {

    private val servoPort = config.property("gpio.servoPort").getString().toInt()
    private val servoSensorPort = config.property("gpio.servoSenorPort").getString().toInt()
    private val doorSensorPort = config.property("gpio.doorSensorPort").getString().toInt()
    private val unlockSwitchPort = config.property("gpio.unlockSwitchPort").getString().toInt()
    private val doorOpenIndicatorPort = config.property("gpio.doorOpenIndicatorPort").getString().toInt()
    private val doorLockIndicatorPort = config.property("gpio.doorLockIndicatorPort").getString().toInt()

    private val servoLockPosition = config.property("gpio.servoLockPosition").getString().toInt()
    private val servoUnlockPosition = config.property("gpio.servoUnlockPosition").getString().toInt()
    private val servoWaitTime = config.property("gpio.servoMoveWaitTimeMilli").getString().toInt()

    private val pi4jContext = Pi4J.newAutoContext()
    private val pwm: Pwm = pi4jContext.create(
        Pwm.newConfigBuilder(pi4jContext)
            .id("BCM$servoPort")
            .address(servoPort)
            .pwmType(PwmType.HARDWARE)
            .frequency(50)
    )

    private val openIndicator = pi4jContext.dout<DigitalOutputProvider>().create("BCM$doorOpenIndicatorPort")
        .apply { config().shutdownState(DigitalState.LOW) }
    private val lockIndicator = pi4jContext.dout<DigitalOutputProvider>().create("BCM$doorLockIndicatorPort")
        .apply { config().shutdownState(DigitalState.LOW) }
    private val servoSensor = pi4jContext.din<DigitalInputProvider>().create("BCM$servoSensorPort").apply {
        addListener(onDoorLockStateChange)
    }
    private val doorSensor = pi4jContext.din<DigitalInputProvider>().create("BCM$doorSensorPort").apply {
        addListener(onDoorOpenStateChange)
    }
    private val unlockSwitch = pi4jContext.din<DigitalInputProvider>().create("BCM$unlockSwitchPort").apply {
        addListener(unlockSwitchListener)
    }

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

    private val unlockSwitchListener = DigitalStateChangeListener { event ->
        if(event?.state() == DigitalState.LOW)
            unlock()
    }


    private var job: Job? = null

    private fun cancelJob() {
        job?.cancel()
        job = null
    }

    private fun moveServo(position: Number): Job = CoroutineScope(Dispatchers.Default).launch {
        pwm.setDutyCycle(position)
        delay(servoWaitTime.milliseconds)
        pwm.setDutyCycle(0)
    }

    override fun unlock() {
        cancelJob()
        job = moveServo(servoUnlockPosition)
    }

    override fun lock() {
        if(getStatus()?.isClose == true) {
            cancelJob()
            job = moveServo(servoLockPosition)
        }
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