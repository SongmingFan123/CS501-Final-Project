package ch.virt.smartphonemouse.ui.connect.dialog

import android.bluetooth.BluetoothClass
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothDiscoverer.DiscoveredDevice
import com.google.android.material.textfield.TextInputLayout

// This class contains the sub page of the add dialog that lets the user add their own custom device by their bluetooth mac address.
class AddManualSubdialog: Fragment(R.layout.subdialog_add_manual) {
    var nameLayout: TextInputLayout? = null
    var macLayout: TextInputLayout? = null

    // Checks whether the current entered name and mac bluetooth address are correct.
    fun check(): Boolean {
        var valid = true
        if (nameLayout!!.editText!!.text == null || nameLayout!!.editText!!.text.toString() == "") {
            nameLayout!!.isErrorEnabled = true
            nameLayout!!.error = getString(R.string.dialog_add_manual_name_error)
            valid = false
        } else {
            nameLayout!!.isErrorEnabled = false
        }
        if (macLayout!!.editText!!.text == null || !macLayout!!.editText!!.text.toString()
                .matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex())
        ) {
            macLayout!!.isErrorEnabled = true
            macLayout!!.error = getString(R.string.dialog_add_manual_mac_error)
            valid = false
        } else {
            macLayout!!.isErrorEnabled = false
        }
        return valid
    }

    // Returns a discovered device, created from the entered details.
    fun createDevice(): DiscoveredDevice {
        return DiscoveredDevice(
            nameLayout!!.editText!!.text.toString(),
            macLayout!!.editText!!.text.toString(),
            BluetoothClass.Device.Major.UNCATEGORIZED
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameLayout = view.findViewById(R.id.add_manual_name)
        macLayout = view.findViewById(R.id.add_manual_mac)
    }
}