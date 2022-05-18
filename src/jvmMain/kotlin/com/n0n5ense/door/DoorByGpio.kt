package com.n0n5ense.door

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.*
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import com.pi4j.plugin.mock.platform.MockPlatform
import com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogInputProvider
import com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogOutputProvider
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProvider
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider
import com.pi4j.plugin.mock.provider.i2c.MockI2CProvider
import com.pi4j.plugin.mock.provider.pwm.MockPwmProvider
import com.pi4j.plugin.mock.provider.serial.MockSerialProvider
import com.pi4j.plugin.mock.provider.spi.MockSpiProvider
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalInputProvider
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider
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

    private val servoLockPosition = config.property("gpio.servoLockPosition").getString().toFloat()
    private val servoUnlockPosition = config.property("gpio.servoUnlockPosition").getString().toFloat()
    private val servoWaitTime = config.property("gpio.servoMoveWaitTimeMilli").getString().toInt()

    private val isMock = config.property("gpio.mock").getString().toBoolean()

    private val mockContext by lazy {
        Pi4J.newContextBuilder()
            .add(MockPlatform())
            .add(
                MockAnalogInputProvider.newInstance(),
                MockAnalogOutputProvider.newInstance(),
                MockSpiProvider.newInstance(),
                MockPwmProvider.newInstance(),
                MockSerialProvider.newInstance(),
                MockI2CProvider.newInstance(),
                MockDigitalInputProvider.newInstance(),
                MockDigitalOutputProvider.newInstance())
            .build()
    }

    private val pi4jContext = if(isMock) mockContext else Pi4J.newAutoContext()
    private val pwm: Pwm = pi4jContext.create(
        Pwm.newConfigBuilder(pi4jContext)
            .id("BCM$servoPort")
            .name("servo")
            .address(servoPort)
            .pwmType(PwmType.HARDWARE)
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
            .address(servoSensorPort)
    )

    private val doorSensor = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .address(doorSensorPort)
    )

    private val unlockSwitch = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
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

    private val unlockSwitchListener = DigitalStateChangeListener { event ->
        if(event?.state() == DigitalState.LOW) {
            unlock()
        }
    }


    init {
        servoSensor.addListener(onDoorLockStateChange)
        doorSensor.addListener(onDoorOpenStateChange)
        unlockSwitch.addListener(unlockSwitchListener)
    }

    private var job: Job? = null

    private fun cancelJob() {
        job?.cancel()
        job = null
    }

    private fun moveServo(position: Number): Job = CoroutineScope(Dispatchers.Default).launch {
        pwm.on(position)
        delay(servoWaitTime.milliseconds)
        pwm.off()
    }

    override fun unlock() {
        cancelJob()
        job = moveServo(servoUnlockPosition)
    }

    override fun lock(force: Boolean) {
        if(getStatus()?.isClose == true || force) {
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