package ch.virt.smartphonemouse.ui.connect.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R

// This class holds the sub page of the add dialog that is displayed when the user should enable location in order to be able to discover near devices.
class AddRequestSettingSubdialog: Fragment(R.layout.subdialog_add_requestsetting) {
    private var error: TextView? = null

    // Shows the error on the screen.
    fun showError() {
        error!!.post { error!!.visibility = View.VISIBLE }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error = view.findViewById(R.id.add_request_setting_error)
    }
}