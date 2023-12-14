package ch.virt.smartphonemouse.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.DeviceStorage
import com.google.android.material.snackbar.Snackbar

/**
 * This fragment shows the settings that are used to configure the settings regarding communication.
 */
class SettingsCommunicationSubfragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_communication, null)
        val communicationRemoveDevices = findPreference<Preference>("communicationRemoveDevices")
        communicationRemoveDevices!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                removeDevices()
                true
            }
    }

    /**
     * Shows the dialog where the user can choose to remove all known devices.
     */
    private fun removeDevices() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.settings_communication_removeall_dialog_message)
            .setPositiveButton(R.string.settings_communication_removeall_dialog_remove) { dialog: DialogInterface?, id: Int ->
                val editor = PreferenceManager.getDefaultSharedPreferences(
                    requireContext()
                ).edit()
                editor.putString(
                    DeviceStorage.Companion.DEVICES_KEY,
                    "[]"
                ) // Reset to an empty json array
                editor.apply()
                Snackbar.make(
                    requireView(),
                    resources.getString(R.string.settings_communication_removeall_confirmation),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(R.string.settings_communication_removeall_dialog_cancel) { dialog: DialogInterface?, id: Int -> }
        val dialog: Dialog = builder.create()
        dialog.show()
    }
}