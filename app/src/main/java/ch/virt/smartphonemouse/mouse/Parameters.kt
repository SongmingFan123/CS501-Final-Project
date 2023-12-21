package ch.virt.smartphonemouse.mouse

import android.content.SharedPreferences

private const val TAG = "Parameters"
class Parameters(private val prefs: SharedPreferences) {

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