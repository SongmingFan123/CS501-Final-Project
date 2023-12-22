package ch.virt.ezController.ui.connect.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.ezController.R

// This class holds a sub page for the add dialog when the user must grant the app permissions to discover new devices.
class AddRequestPermissionSubdialog: Fragment(R.layout.subdialog_add_requestpermission) {
    private var error: TextView? = null

    // Displays the error on the screen.
    fun showError() {
        error!!.post { error!!.visibility = View.VISIBLE }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error = view.findViewById(R.id.add_request_permission_error)
    }
}