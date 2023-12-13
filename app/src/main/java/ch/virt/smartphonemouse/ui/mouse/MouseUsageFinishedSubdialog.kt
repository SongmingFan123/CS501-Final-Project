package ch.virt.smartphonemouse.ui.mouse

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R

/**
 * This sub dialog is the last page of the usage dialog. It gives the user the option to disable every further usage dialog.
 */
class MouseUsageFinishedSubdialog
/**
 * Creates the sub dialog.
 */
    : Fragment(R.layout.subdialog_mouse_usage_finished) {
    private var notAgain: CheckBox? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notAgain = view.findViewById(R.id.mouse_usage_finished_notagain)
        notAgain?.setChecked(
            !PreferenceManager.getDefaultSharedPreferences(
                context
            ).getBoolean("showUsage", true)
        )
        notAgain?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            PreferenceManager.getDefaultSharedPreferences(
                context
            ).edit().putBoolean("showUsage", !isChecked).apply()
        })
    }
}