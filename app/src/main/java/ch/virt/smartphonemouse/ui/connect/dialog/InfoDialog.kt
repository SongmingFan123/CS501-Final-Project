package ch.virt.smartphonemouse.ui.connect.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.transmission.HostDevice
import java.text.SimpleDateFormat
import java.util.Date

/**
 * This dialog is shown when the user wants to see more information about a device.
 */
class InfoDialog
/**
 * Creates the info dialog.
 *
 * @param bluetooth bluetooth handler to remove device from
 * @param device    device that is viewed
 */(private val bluetooth: BluetoothHandler?, private val device: HostDevice) : DialogFragment() {
    private var dismissListener: DialogInterface.OnDismissListener? = null

    /**
     * Populates the text views on the view with the data of the device.
     *
     * @param view view to populate
     */
    private fun populate(view: View) {
        (view.findViewById<View>(R.id.info_address) as TextView).text = device.address
        (view.findViewById<View>(R.id.info_name) as TextView).text = device.name
    }

    /**
     * Sets the dismiss listener.
     * The listener should be set before the dialog is shown.
     *
     * @param dismissListener dismiss listener that will be passed to the dialog
     * @see Dialog.setOnDismissListener
     */
    fun setOnDismissListener(dismissListener: DialogInterface.OnDismissListener?) {
        this.dismissListener = dismissListener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener!!.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_info, null)
        populate(view)
        builder.setView(view)
            .setPositiveButton(R.string.dialog_info_positive, null)
            .setNeutralButton(R.string.dialog_info_delete) { dialog: DialogInterface?, which: Int ->
                bluetooth?.devices?.removeDevice(
                    device.address
                )
            }
        val dialog: Dialog = builder.create()
        dialog.setTitle(R.string.dialog_info_title)
        return dialog
    }
}