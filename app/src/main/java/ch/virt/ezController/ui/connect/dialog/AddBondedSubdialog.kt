package ch.virt.ezController.ui.connect.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.ezController.R
import ch.virt.ezController.transmission.BluetoothDiscoverer.DiscoveredDevice
import ch.virt.ezController.transmission.BluetoothHandler

// This class contains the sub page of the add dialog.
class AddBondedSubdialog (private val handler: BluetoothHandler?, private val target: DiscoveredDevice?) :
    Fragment(R.layout.subdialog_add_bonded) {
    private var error: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error = view.findViewById(R.id.add_bonded_error)
    }
}