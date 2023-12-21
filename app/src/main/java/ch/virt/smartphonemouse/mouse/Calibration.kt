package ch.virt.smartphonemouse.mouse

import android.util.Log
import ch.virt.smartphonemouse.mouse.components.WindowAverage
import ch.virt.smartphonemouse.mouse.math.Vec3f

private const val TAG = "Calibration"
// For mouse movement calibration
class Calibration(private var listener: StateListener, private val params: Parameters) {
    var state = STATE_START
    var started = false
    var startTime = 0f
    private var samples = 0
    private var accelerationNoise: MutableList<Float>? = null
    private var rotationNoise: MutableList<Float>? = null
    private var gravityAverage: WindowAverage? = null
    private var noiseAverage: WindowAverage? = null
    private var durationSampling = 0f
    private var durationNoise = 0f
    fun setListener(listener: StateListener) {
        this.listener = listener
    }

    fun startCalibration() {
        Log.d(TAG, "Starting Sampling Rate Calibration")
        samples = 0
        durationSampling = params.calibrationSamplingTime
        updateState(STATE_SAMPLING)
    }

    private fun startNoise() {
        Log.d(TAG, "Starting measuring noise")
        val samplingRate = samples / durationSampling
        params.calibrateSamplingRate(samplingRate)
        gravityAverage = WindowAverage(params.lengthWindowGravity)
        noiseAverage = WindowAverage(params.lengthWindowNoise)
        accelerationNoise = ArrayList()
        rotationNoise = ArrayList()
        durationNoise = params.calibrationNoiseTime
        updateState(STATE_NOISE)
    }

    private fun endCalibration() {
        Log.d(TAG, "Ending Calibration")
        params.measureNoise(accelerationNoise, rotationNoise)
        params.isCalibrated = true
        updateState(STATE_END)
    }

    fun data(time: Float, acceleration: Vec3f, angularVelocity: Vec3f) {
        if (!started) {
            startTime = time
            started = true
        }
        if (state == STATE_SAMPLING) {
            if (time - startTime > durationSampling) {
                startNoise()
            }
            samples++
        } else if (state == STATE_NOISE) {
            if (time - startTime > durationNoise) {
                endCalibration()
            }
            var acc = acceleration.xy().length()

            val gravity = gravityAverage!!.avg(acc)
            acc -= gravity
            acc = Math.abs(acc)

            // Remove noise
            acc = noiseAverage!!.avg(acc)

            // Calculate the rotation activation
            val rot = Math.abs(angularVelocity.z)
            accelerationNoise!!.add(acc)
            rotationNoise!!.add(rot)
        }
    }

    private fun updateState(state: Int) {
        this.state = state
        started = false
        listener.update(state)
    }

    interface StateListener {
        fun update(state: Int)
    }

    companion object {
        const val STATE_START = 0
        const val STATE_SAMPLING = 1
        const val STATE_NOISE = 3
        const val STATE_END = 4
    }
}