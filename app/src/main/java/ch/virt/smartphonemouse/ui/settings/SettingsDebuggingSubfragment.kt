package ch.virt.smartphonemouse.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ch.virt.smartphonemouse.R

/**
 * This fragment is the settings page, where the user can configure everything regarding the movement.
 */
class SettingsDebuggingSubfragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.settings_debugging, null)
        findPreference<Preference>("debugDownload")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        context!!.getText(R.string.settings_debug_server_download_url).toString()
                    )
                )
                startActivity(browserIntent)
                false
            }
    }
}