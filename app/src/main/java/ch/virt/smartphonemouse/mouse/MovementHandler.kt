package ch.virt.smartphonemouse.mouse

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.mouse.math.Vec3f
import ch.virt.smartphonemouse.transmission.DebugTransmitter

/**
 * This class handles and calculates the movement of the mouse
 */
class MovementHandler(private val context: Context, private val inputs: MouseInputs) :
    SensorEventListener {
    private var manager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var registered = false
    private var gyroSample =
        Vec3f() // TODO: Make this a buffer to accommodate for vastly different sampling rates
    private var lastTime: Long = 0
    private var firstTime: Long = 0
    private var processing: Processing? = null

    /**
     * Creates a movement handler.
     *
     * @param context context to get the sensor from
     * @param inputs  inputs to send the movement to
     */
    init {
        fetchSensor()
    }

    /**
     * Creates the signal processing pipelines.
     */
    fun create(debug: DebugTransmitter?) {
        processing = Processing(
            debug, Parameters(
                PreferenceManager.getDefaultSharedPreferences(
                    context
                )
            )
        )
    }

    /**
     * Fetches the sensor from the context.
     */
    private fun fetchSensor() {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = manager!!.getDefaultSensor(SENSOR_TYPE_ACCELEROMETER)
        gyroscope = manager!!.getDefaultSensor(SENSOR_TYPE_GYROSCOPE)
    }

    /**
     * Registers the sensor for this handler.
     */
    fun register() {
        if (registered) return
        manager!!.registerListener(this, accelerometer, SAMPLING_RATE)
        manager!!.registerListener(this, gyroscope, SAMPLING_RATE)
        lastTime = 0
        firstTime = 0
        registered = true
    }

    /**
     * Unregisters the sensor for this handler.
     */
    fun unregister() {
        if (!registered) return
        manager!!.unregisterListener(this, accelerometer)
        manager!!.unregisterListener(this, gyroscope)
        registered = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!registered) return  // Ignore Samples when the listener is not registered
        if (event.sensor.type == SENSOR_TYPE_ACCELEROMETER) {
            if (firstTime == 0L) { // Ignore First sample, because there is no delta
                lastTime = event.timestamp
                firstTime = event.timestamp
                return
            }
            val delta = (event.timestamp - lastTime) * NANO_FULL_FACTOR
            val time = (event.timestamp - firstTime) * NANO_FULL_FACTOR
            val acceleration = Vec3f(event.values[0], event.values[1], event.values[2])
            val distance = processing!!.next(time, delta, acceleration, gyroSample)
            inputs.changeXPosition(distance!!.x)
            inputs.changeYPosition(-distance.y)
            lastTime = event.timestamp
        } else if (event.sensor.type == SENSOR_TYPE_GYROSCOPE) {
            // Here we assume that the samples arrive in chronological order (which is crucial anyway), so we will always have the latest sample in this variable
            gyroSample = Vec3f(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val NANO_FULL_FACTOR = 1e-9f
        const val SENSOR_TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER
        const val SENSOR_TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE
        const val SAMPLING_RATE = SensorManager.SENSOR_DELAY_FASTEST
    }
}