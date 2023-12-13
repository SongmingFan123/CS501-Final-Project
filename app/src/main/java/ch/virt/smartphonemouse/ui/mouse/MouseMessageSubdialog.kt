package ch.virt.smartphonemouse.ui.mouse

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R

/**
 * This sub dialog shows some basic information as text to the user.
 */
class MouseMessageSubdialog
/**
 * Creates the message sub dialog.
 *
 * @param message message that gets shown
 */(private val message: String) : Fragment(R.layout.subdialog_mouse_message) {
    private var messageView: TextView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageView = view.findViewById(R.id.mouse_message_message)
        messageView?.setText(message)
    }
}