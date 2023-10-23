package net.hikali_47041

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.*

open class DoorBell(
    val onButtonPushed: () -> Unit,
    val onBusButtonPushed: () -> Unit
)

class MockDoorBell(onButtonPushed: () -> Unit, onBusButtonPushed: () -> Unit):
    DoorBell(onButtonPushed, onBusButtonPushed) {
    fun pushButton() {
        onButtonPushed.invoke()
    }
}

class RealDoorBell(
    port: Int,
    busButtonPort: Int,
    busLedPort: Int,
    onButtonPushed: () -> Unit,
    onBusButtonPushed: () -> Unit
):
    DoorBell(onButtonPushed, onBusButtonPushed) {
    private val pi4jContext = Pi4J.newAutoContext()

    private val doorButton = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(port)
    )

    private val busButton = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(busButtonPort)
    )

    private val busLed = pi4jContext.create(
        DigitalOutputConfigBuilder.newInstance(pi4jContext)
            .address(busLedPort)
    ).apply { config().shutdownState(DigitalState.LOW) }


    private val onDoorButtonStateChange = DigitalStateChangeListener { event ->
        if(event?.state() == DigitalState.LOW) {
            onButtonPushed.invoke()
        }
    }

    private val onBusButtonStateChange = DigitalStateChangeListener { event ->
        if(event?.state() == DigitalState.LOW) {
            onBusButtonPushed.invoke()
        }
    }

    init {
        doorButton.addListener(onDoorButtonStateChange)
        busButton.addListener(onBusButtonStateChange)
    }

    fun changeBusLedState(isLighting: Boolean) {
        if(isLighting) {
            busLed.state(DigitalState.HIGH)
        } else {
            busLed.state(DigitalState.LOW)
        }
    }


    fun pushButton() {
        onButtonPushed.invoke()
    }
}

