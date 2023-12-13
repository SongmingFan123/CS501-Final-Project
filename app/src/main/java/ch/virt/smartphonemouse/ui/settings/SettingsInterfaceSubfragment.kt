package ch.virt.smartphonemouse.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.ui.settings.custom.SeekFloatPreference
import ch.virt.smartphonemouse.ui.settings.custom.SeekIntegerPreference

/**
 * This fragment is the settings page, where the user can configure things regarding the interface.
 */
class SettingsInterfaceSubfragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.settings_interface, null)
        val interfaceLayoutMiddleWidth =
            findPreference<SeekFloatPreference>("interfaceLayoutMiddleWidth")
        interfaceLayoutMiddleWidth?.maximum = (0.5f)
        interfaceLayoutMiddleWidth?.minimum = (0.0f)
        interfaceLayoutMiddleWidth!!.setStepsAndRelated(100)
        interfaceLayoutMiddleWidth.update()
        val interfaceLayoutHeight = findPreference<SeekFloatPreference>("interfaceLayoutHeight")
        interfaceLayoutHeight?.maximum = (1.0f)
        interfaceLayoutHeight?.minimum = (0.0f)
        interfaceLayoutHeight!!.setStepsAndRelated(100)
        interfaceLayoutHeight.update()
        val interfaceVibrationsSpecialIntensity =
            findPreference<SeekIntegerPreference>("interfaceVibrationsSpecialIntensity")
        interfaceVibrationsSpecialIntensity?.maximum = 100
        interfaceVibrationsSpecialIntensity?.minimum = 0
        interfaceVibrationsSpecialIntensity!!.setSteps(100)
        interfaceVibrationsSpecialIntensity.update()
        val interfaceVibrationsScrollIntensity =
            findPreference<SeekIntegerPreference>("interfaceVibrationsScrollIntensity")
        interfaceVibrationsScrollIntensity?.maximum = 100
        interfaceVibrationsScrollIntensity?.minimum = 0
        interfaceVibrationsScrollIntensity!!.setSteps(100)
        interfaceVibrationsScrollIntensity.update()
        val interfaceVibrationsButtonIntensity =
            findPreference<SeekIntegerPreference>("interfaceVibrationsButtonIntensity")
        interfaceVibrationsButtonIntensity?.maximum = 100
        interfaceVibrationsButtonIntensity?.minimum = 0
        interfaceVibrationsButtonIntensity!!.setSteps(100)
        interfaceVibrationsButtonIntensity.update()
        val interfaceVisualsIntensity =
            findPreference<SeekFloatPreference>("interfaceVisualsIntensity")
        interfaceVisualsIntensity?.maximum = 1.0f
        interfaceVisualsIntensity?.minimum = 0.0f
        interfaceVisualsIntensity!!.setStepsAndRelated(100)
        interfaceVisualsIntensity.update()
        checkAdvanced()
    }

    fun checkAdvanced() {
        val advanced =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("advanced", false)
        findPreference<Preference>("interfaceBehaviour")!!.isVisible = advanced
        findPreference<Preference>("interfaceVisualsStrokeWeight")!!.isVisible = advanced
        findPreference<Preference>("interfaceVisualsIntensity")!!.isVisible =
            advanced
        findPreference<Preference>("interfaceVibrationsButtonIntensity")!!.isVisible = advanced
        findPreference<Preference>("interfaceVibrationsButtonLength")!!.isVisible = advanced
        findPreference<Preference>("interfaceVibrationsScrollIntensity")!!.isVisible = advanced
        findPreference<Preference>("interfaceVibrationsScrollLength")!!.isVisible =
            advanced
        findPreference<Preference>("interfaceVibrationsSpecialIntensity")!!.isVisible = advanced
        findPreference<Preference>("interfaceVibrationsSpecialLength")!!.isVisible =
            advanced
        findPreference<Preference>("interfaceLayout")!!.isVisible = advanced
    }

}