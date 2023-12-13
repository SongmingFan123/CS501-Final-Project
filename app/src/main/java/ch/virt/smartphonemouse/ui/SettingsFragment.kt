package ch.virt.smartphonemouse.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.customization.DefaultSettings
import com.google.android.material.snackbar.Snackbar

/**
 * This fragment contains the settings for the app.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, null)
        val reset = findPreference<Preference>("reset")
        reset!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                resetSettings()
                true
            }
        checkAdvanced(
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("advanced", false)
        )
        findPreference<Preference>("advanced")!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                checkAdvanced(newValue as Boolean)
                true
            }
    }

    fun checkAdvanced(advanced: Boolean) {
        findPreference<Preference>("reset")!!.isVisible = advanced
        findPreference<Preference>("debugging")!!.isVisible = advanced
    }

    /**
     * Shows the settings reset dialog, where the user can choose to restore their settings.
     */
    private fun resetSettings() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.settings_reset_dialog_message)
            .setPositiveButton(R.string.settings_reset_dialog_reset) { dialog: DialogInterface?, id: Int ->
                DefaultSettings.set(
                    PreferenceManager.getDefaultSharedPreferences(
                        context
                    )
                )
                Snackbar.make(
                    requireView(),
                    resources.getString(R.string.settings_reset_confirmation),
                    Snackbar.LENGTH_SHORT
                ).show()
                val advanced = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("advanced", false)
                checkAdvanced(advanced)
                (findPreference<Preference>("advanced") as SwitchPreference?)!!.isChecked = advanced
            }
            .setNegativeButton(R.string.settings_reset_dialog_cancel) { dialog: DialogInterface?, id: Int -> }
        val dialog: Dialog = builder.create()
        dialog.show()
    }
}