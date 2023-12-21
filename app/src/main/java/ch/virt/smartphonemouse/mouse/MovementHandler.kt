package ch.virt.smartphonemouse.mouse

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.mouse.math.Vec3f
import ch.virt.smartphonemouse.transmission.DebugTransmitter

private const val TAG = "Movement Handler"
// This class handles and calculates the movement of the mouse
class MovementHandler(private val context: Context, private val inputs: MouseInputs) :
    SensorEventListener {
    private var manager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var gyroSample = Vec3f()
    private var registered = false
    private var lastTime: Long = 0
    private var firstTime: Long = 0
    private var processing: Processing? = null

    init {
        fetchSensor()
    }

    // Creates the signal processing pipelines.
    fun create(debug: DebugTransmitter?) {
        processing = Processing(
            debug, Parameters(
                PreferenceManager.getDefaultSharedPreferences(
                    context
                )
            )
        )
    }

    // Fetches the sensor from the context.
    private fun fetchSensor() {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = manager!!.getDefaultSensor(SENSOR_TYPE_ACCELEROMETER)
        gyroscope = manager!!.getDefaultSensor(SENSOR_TYPE_GYROSCOPE)
    }

    // Registers the sensor for this handler.
    fun register() {
        if (registered) return
        manager!!.registerListener(this, accelerometer, SAMPLING_RATE)
        manager!!.registerListener(this, gyroscope, SAMPLING_RATE)
        lastTime = 0
        firstTime = 0
        registered = true
    }

    // Unregisters the sensor for this handler.
    fun unregister() {
        if (!registered) return
        manager!!.unregisterListener(this, accelerometer)
        manager!!.unregisterListener(this, gyroscope)
        registered = false
    }
    //handle sensor change given sensor event
    override fun onSensorChanged(event: SensorEvent) {
        if (!registered) return
        if (event.sensor.type == SENSOR_TYPE_ACCELEROMETER) {
            if (firstTime == 0L) {
                lastTime = event.timestamp
                firstTime = event.timestamp
                return
            }
            val delta = (event.timestamp - lastTime) * NANO_FULL_FACTOR
            val time = (event.timestamp - firstTime) * NANO_FULL_FACTOR
            val acceleration = Vec3f(event.values[0], event.values[1], event.values[2])
//            Log.d(TAG, "" + gyroSample.x + " " + gyroSample.y + " " + gyroSample.z)
            val distance = processing!!.step(time, delta, acceleration, gyroSample)
            inputs.changeXPosition(distance!!.x)
            inputs.changeYPosition(-distance.y)
            lastTime = event.timestamp
        }
        else if (event.sensor.type == SENSOR_TYPE_GYROSCOPE) {
            gyroSample = Vec3f(event.values[0], event.values[1], event.values[2])
        }
    }
    //accuracy change handler
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val NANO_FULL_FACTOR = 1e-9f
        const val SENSOR_TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER
        const val SENSOR_TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE
        const val SAMPLING_RATE = SensorManager.SENSOR_DELAY_FASTEST
    }
}