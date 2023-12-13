package ch.virt.smartphonemouse.mouse

import android.content.Context
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.transmission.BluetoothHandler

/**
 * This class collects the calculated inputs and transmits them to the computer when needed.
 */
class MouseInputs
/**
 * Creates this class.
 *
 * @param bluetoothHandler bluetooth handler to send the signals to
 * @param context          context to fetch settings from
 */(private val bluetoothHandler: BluetoothHandler, private val context: Context) {
    private val NANO_TO_FULL = 1e9f
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

    /**
     * Starts the transmission to the pc.
     */
    fun start() {
        if (running) return
        thread = Thread { this.run() }
        transmissionRate = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("communicationTransmissionRate", 100)
        running = true
        thread!!.start()
    }

    /**
     * Starts the sending loop.
     * Should be executed on a separate thread.
     */
    private fun run() {
        lastTime = System.nanoTime()
        while (running) {
            val current = System.nanoTime()
            if (current - lastTime >= NANO_TO_FULL / transmissionRate) {
                sendUpdate()
                lastTime = current
            }
        }
    }

    /**
     * Stops the transmission to the pc.
     */
    fun stop() {
        running = false
    }

    /**
     * Sends an update to the pc.
     */
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

        // Reset Deltas
        xPosition -= x.toFloat()
        yPosition -= y.toFloat()
        wheelPosition = 0
    }

    /**
     * Changes the current wheel position.
     *
     * @param delta change in wheel steps
     */
    fun changeWheelPosition(delta: Int) {
        wheelPosition += delta
    }

    /**
     * Sets the current state of the left button.
     *
     * @param leftButton whether the left button is pressed
     */
    fun setLeftButton(leftButton: Boolean) {
        this.leftButton = leftButton
    }

    /**
     * Sets the current state of the middle button.
     *
     * @param middleButton whether the middle button is pressed
     */
    fun setMiddleButton(middleButton: Boolean) {
        this.middleButton = middleButton
    }

    /**
     * Sets the current state of the right button.
     *
     * @param rightButton whether the right button is pressed
     */
    fun setRightButton(rightButton: Boolean) {
        this.rightButton = rightButton
    }

    /**
     * Changes the x position of the mouse
     *
     * @param x change of the position
     */
    fun changeXPosition(x: Float) {
        xPosition += x
    }

    /**
     * Changes the y position of the mouse
     *
     * @param y change of the position
     */
    fun changeYPosition(y: Float) {
        yPosition += y
    }
}