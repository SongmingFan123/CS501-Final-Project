package ch.virt.smartphonemouse.mouse

import android.content.SharedPreferences

private const val TAG = "Parameters"
class Parameters(private val prefs: SharedPreferences) {
    var isCalibrated: Boolean
        get() = prefs.getBoolean("movementCalibrated", false)
        set(calibrated) {
            prefs.edit().putBoolean("movementCalibrated", calibrated).apply()
        }
    val calibrationNoiseTime: Float
        get() = prefs.getFloat("movementCalibrationNoise", CALIBRATION_NOISE)
    val calibrationSamplingTime: Float
        get() = prefs.getFloat("movementCalibrationSampling", CALIBRATION_SAMPLING)

    fun calibrateSamplingRate(samplingRate: Float) {
        val edit = prefs.edit()
        edit.putFloat("movementSampling", samplingRate)
        edit.apply()
    }

    fun measureNoise(accelerationNoise: MutableList<Float>?, rotationNoise: MutableList<Float>?) {
        accelerationNoise?.sortWith { obj: Float, anotherFloat: Float ->
            obj.compareTo(anotherFloat)
        }
        rotationNoise?.sortWith { obj: Float, anotherFloat: Float? ->
            obj.compareTo(
                anotherFloat!!
            )
        }

        var accelerationSample = accelerationNoise!![((accelerationNoise.size - 1) * prefs.getFloat(
            "movementNoiseRatioAcceleration",
            NOISE_RATIO_ACCELERATION
        )).toInt()]
        accelerationSample *= prefs.getFloat(
            "movementNoiseFactorAcceleration",
            NOISE_FACTOR_ACCELERATION
        )

        var rotationSample = rotationNoise!![((rotationNoise.size - 1) * prefs.getFloat(
            "movementNoiseRatioRotation",
            NOISE_RATIO_ROTATION
        )).toInt()]
        rotationSample *= prefs.getFloat("movementNoiseFactorRotation", NOISE_FACTOR_ROTATION)

        val edit = prefs.edit()
        edit.putFloat("movementThresholdAcceleration", accelerationSample)
        edit.putFloat("movementThresholdRotation", rotationSample)
        edit.apply()
    }

    val lengthWindowGravity: Int
        get() = Math.round(
            prefs.getFloat("movementDurationWindowGravity", DURATION_WINDOW_GRAVITY)
                    * prefs.getFloat("movementSampling", 500f)
        )
    val lengthWindowNoise: Int
        get() = Math.round(
            prefs.getFloat("movementDurationWindowNoise", DURATION_WINDOW_NOISE)
                    * prefs.getFloat("movementSampling", 500f)
        )
    val lengthThreshold: Int
        get() = Math.round(
            prefs.getFloat("movementDurationThreshold", DURATION_THRESHOLD)
                    * prefs.getFloat("movementSampling", 500f)
        )
    val lengthGravity: Int
        get() = Math.round(
            prefs.getFloat("movementDurationGravity", DURATION_GRAVITY)
                    * prefs.getFloat("movementSampling", 500f)
        )
    val sensitivity: Float
        get() = prefs.getFloat("movementSensitivity", SENSITIVITY)
    val thresholdAcceleration: Float
        get() = prefs.getFloat("movementThresholdAcceleration", 0.03f)
    val thresholdRotation: Float
        get() = prefs.getFloat("movementThresholdRotation", 0.01f)
    val enableGravityRotation: Boolean
        get() = prefs.getBoolean("movementEnableGravityRotation", ENABLE_GRAVITY_ROTATION)

    companion object {
        private const val CALIBRATION_SAMPLING = 5f
        private const val CALIBRATION_NOISE = 10f
        private const val NOISE_RATIO_ACCELERATION = 1f
        private const val NOISE_RATIO_ROTATION = 1f
        private const val NOISE_FACTOR_ACCELERATION = 1.2f
        private const val NOISE_FACTOR_ROTATION = 1.2f
        private const val DURATION_THRESHOLD = 0.1f
        private const val DURATION_WINDOW_GRAVITY = 0.02f
        private const val DURATION_WINDOW_NOISE = 0.01f
        private const val DURATION_GRAVITY = 2f
        private const val SENSITIVITY = 15000f
        private const val ENABLE_GRAVITY_ROTATION = false
    }
}