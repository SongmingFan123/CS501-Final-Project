package ch.virt.smartphonemouse.ui.settings

import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.ui.settings.custom.EditFloatPreference
import ch.virt.smartphonemouse.ui.settings.custom.SeekFloatPreference
import ch.virt.smartphonemouse.ui.settings.dialog.CalibrateDialog

/**
 * This fragment is the settings page, where the user can configure everything regarding the movement.
 */
class SettingsMovementSubfragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.settings_movement, null)
        val movementSensitivity = findPreference<SeekFloatPreference>("movementSensitivity")
        movementSensitivity?.maximum = (20000f)
        movementSensitivity?.minimum = (10000f)
        movementSensitivity!!.steps = 200
        movementSensitivity.update()
        val rotThreshold = findPreference<EditFloatPreference>("movementThresholdRotation")
        val accThreshold = findPreference<EditFloatPreference>("movementThresholdAcceleration")
        val samplingRate = findPreference<EditFloatPreference>("movementSampling")
        val movementSamplingCalibrate = findPreference<Preference>("movementRecalibrate")
        movementSamplingCalibrate!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                val dialog = CalibrateDialog()
                dialog.setFinishedListener { dialog1: DialogInterface? ->
                    // Update changed values after calibration
                    rotThreshold!!.update()
                    accThreshold!!.update()
                    samplingRate!!.update()
                }
                dialog.show(this@SettingsMovementSubfragment.parentFragmentManager, null)
                true
            }
        checkAdvanced()
    }

    fun checkAdvanced() {
        val advanced =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("advanced", false)
        findPreference<Preference>("movementEnableGravityRotation")!!.isVisible = advanced
        findPreference<Preference>("movementCalibrationSampling")!!.isVisible =
            advanced
        findPreference<Preference>("movementCalibrationNoise")!!.isVisible = advanced
        findPreference<Preference>("movementNoiseRatioAcceleration")!!.isVisible = advanced
        findPreference<Preference>("movementNoiseFactorAcceleration")!!.isVisible = advanced
        findPreference<Preference>("movementNoiseRatioRotation")!!.isVisible =
            advanced
        findPreference<Preference>("movementNoiseFactorRotation")!!.isVisible =
            advanced
        findPreference<Preference>("movementThresholdAcceleration")!!.isVisible = advanced
        findPreference<Preference>("movementThresholdRotation")!!.isVisible = advanced
        findPreference<Preference>("movementSampling")!!.isVisible = advanced
        findPreference<Preference>("movementDurationWindowGravity")!!.isVisible = advanced
        findPreference<Preference>("movementDurationWindowNoise")!!.isVisible = advanced
        findPreference<Preference>("movementDurationThreshold")!!.isVisible =
            advanced
        findPreference<Preference>("movementDurationGravity")!!.isVisible = advanced
    }
}