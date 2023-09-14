package com.n0n5ense

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.io.gpio.digital.PullResistance
import kotlinx.coroutines.*

class RebootService(
    pin: Int,
    private val pushDelay: Long
) {

    private val pi4jContext = Pi4J.newAutoContext()

    private var job: Job? = null

    private val rebootButton = pi4jContext.create(
        DigitalInputConfigBuilder.newInstance(pi4jContext)
            .pull(PullResistance.PULL_UP)
            .address(pin)
    )

    private val onRebootButtonStateChange = DigitalStateChangeListener { event ->
        when(event?.state()) {
            DigitalState.LOW -> {
                job?.cancel()
                job = CoroutineScope(Dispatchers.Default).launch {
                    delay(pushDelay)
                    reboot()
                }
            }
            DigitalState.HIGH -> {
                job?.cancel()
            }
            else -> {}
        }
    }

    init {
        rebootButton.addListener(onRebootButtonStateChange)
    }

    private fun reboot() {
        Runtime.getRuntime().exec("reboot now")
    }

}