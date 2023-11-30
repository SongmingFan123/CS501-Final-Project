package ch.virt.smartphonemouse.ui.connect.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothDiscoverer.DiscoveredDevice
import ch.virt.smartphonemouse.transmission.BluetoothHandler

/**
 * This class contains the sub page of the add dialog, which is displayed when a device is bonded with the smartphone.
 */
class AddBondedSubdialog
/**
 * Creates the sub dialog.
 *
 * @param handler handler to check whether still bonded
 * @param target  target device to check for
 */(private val handler: BluetoothHandler?, private val target: DiscoveredDevice?) :
    Fragment(R.layout.subdialog_add_bonded) {
    private var error: TextView? = null

    /**
     * Checks if the device is still bonded.
     * If it is still bonded, an error message will be displayed.
     *
     * @return whether it is NOT bonded
     */
    fun check(): Boolean {
        return if (handler!!.isBonded(target?.address)) {
            error!!.visibility = View.VISIBLE
            false
        } else {
            error!!.visibility = View.GONE
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error = view.findViewById(R.id.add_bonded_error)
    }
}