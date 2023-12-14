package ch.virt.smartphonemouse.customization

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.mouse.Calibration
import ch.virt.smartphonemouse.mouse.Calibration.StateListener
import ch.virt.smartphonemouse.mouse.MovementHandler
import ch.virt.smartphonemouse.mouse.Parameters
import ch.virt.smartphonemouse.mouse.math.Vec3f

/**
 * This class is used to measure and save the sampling rate of the inbuilt accelerometer.
 */
class CalibrationHandler(private val context: Context?) : SensorEventListener {
    private var manager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var begun = false
    private var firstTime: Long = 0
    private var gyroSample = Vec3f()
    private var registered = false
    private val calibration: Calibration

    /**
     * Creates the calibrator.
     *
     * @param context context to use
     */
    init {
        val stl = object : StateListener {
            override fun update(state: Int) {}
        }
        calibration = Calibration(
            stl, Parameters(
                PreferenceManager.getDefaultSharedPreferences(
                    context!!
                )
            )
        )
        fetchSensor()
    }

    /**
     * Fetches the sensor from the system.
     */
    private fun fetchSensor() {
        manager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer =
            manager!!.getDefaultSensor(MovementHandler.Companion.SENSOR_TYPE_ACCELEROMETER)
        gyroscope = manager!!.getDefaultSensor(MovementHandler.Companion.SENSOR_TYPE_GYROSCOPE)
    }

    /**
     * Registers itself as a listener.
     */
    private fun register() {
        if (registered) return
        manager!!.registerListener(this, accelerometer, MovementHandler.Companion.SAMPLING_RATE)
        manager!!.registerListener(this, gyroscope, MovementHandler.Companion.SAMPLING_RATE)
        registered = true
    }

    /**
     * Unregisters itself as a listener.
     */
    private fun unregister() {
        if (!registered) return
        manager!!.unregisterListener(this, accelerometer)
        manager!!.unregisterListener(this, gyroscope)
        registered = false
    }

    /**
     * Starts the measuring process.
     *
     * @param doneListener listener that is executed once the process has finished
     */
    fun calibrate(doneListener: StateListener) {
        val stl = object : StateListener {
            override fun update(state: Int) {
                if (state == Calibration.Companion.STATE_END) unregister()
                doneListener.update(state)
            }
        }
        calibration.setListener(stl)
        begun = false
        calibration.startCalibration()
        register()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == MovementHandler.Companion.SENSOR_TYPE_ACCELEROMETER) {
            if (!begun) {
                begun = true
                firstTime = event.timestamp
            }
            val time = (event.timestamp - firstTime) * NANO_FULL_FACTOR
            val acceleration = Vec3f(event.values[0], event.values[1], event.values[2])
            calibration.data(time, acceleration, gyroSample)
        } else if (event.sensor.type == MovementHandler.Companion.SENSOR_TYPE_GYROSCOPE) {
            gyroSample = Vec3f(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val NANO_FULL_FACTOR = 1e-9f
    }
}