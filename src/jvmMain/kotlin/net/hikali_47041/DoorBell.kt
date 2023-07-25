package net.hikali_47041

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.io.gpio.digital.PullResistance

open class DoorBell(
    val onButtonPushed: () -> Unit
)

class MockDoorBell(onButtonPushed: () -> Unit) : DoorBell(onButtonPushed) {
    fun pushButton() {
        onButtonPushed.invoke()
    }
}

class RealDoorBell(port: Int, onButtonPushed: () -> Unit) : DoorBell(onButtonPushed) {
    private val pi4jContext = Pi4J.newAutoContext()

    private val doorButton = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(port)
    )


    private val onDoorButtonStateChange = DigitalStateChangeListener { event ->
        if (event?.state() == DigitalState.LOW) {
            onButtonPushed.invoke()
        }
    }

    init {
        doorButton.addListener(onDoorButtonStateChange)
    }


    fun pushButton() {
        onButtonPushed.invoke()
    }
}

