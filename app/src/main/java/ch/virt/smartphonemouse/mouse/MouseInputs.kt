package ch.virt.smartphonemouse.mouse

import android.content.Context
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.transmission.BluetoothHandler

// This class collects the calculated inputs and transmits them to the computer when needed.
class MouseInputs
(private val bluetoothHandler: BluetoothHandler, private val context: Context) {
    private var xPosition = 0f
    private var yPosition = 0f
    private var wheelPosition = 0
    private var leftButton = false
    private var middleButton = false
    private var rightButton = false
    private var transmissionRate = 0
    private var thread: Thread? = null
    private var running = false
    private var lastTime: Long = 0

    fun start() {
        if (running) return
        thread = Thread { this.run() }
        transmissionRate = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("TransmissionRate", 100)
        running = true
        thread!!.start()
    }

    private fun run() {
        lastTime = System.nanoTime()
        while (running) {
            val current = System.nanoTime()
            if (current - lastTime >= 1e9f / transmissionRate) {
                sendUpdate()
                lastTime = current
            }
        }
    }

    fun stop() {
        running = false
    }

    private fun sendUpdate() {
        val x = xPosition.toInt()
        val y = yPosition.toInt()
        if (bluetoothHandler.host?.isConnected == true) bluetoothHandler.host!!.sendReport(
            leftButton,
            middleButton,
            rightButton,
            wheelPosition,
            x,
            y
        )

        xPosition -= x.toFloat()
        yPosition -= y.toFloat()
        wheelPosition = 0
    }

    fun changeWheelPosition(delta: Int) {
        wheelPosition += delta
    }

    fun setLeftButton(leftButton: Boolean) {
        this.leftButton = leftButton
    }

    fun setMiddleButton(middleButton: Boolean) {
        this.middleButton = middleButton
    }

    fun setRightButton(rightButton: Boolean) {
        this.rightButton = rightButton
    }

    fun changeXPosition(x: Float) {
        xPosition += x
    }

    fun changeYPosition(y: Float) {
        yPosition += y
    }
}