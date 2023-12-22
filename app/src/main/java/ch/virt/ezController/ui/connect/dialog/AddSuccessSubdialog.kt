package ch.virt.ezController.ui.connect.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.ezController.R
import ch.virt.ezController.transmission.BluetoothDiscoverer.DiscoveredDevice

// This class holds the sub page for the add dialog that informs the user that they have successfully added a device.
class AddSuccessSubdialog(private val target: DiscoveredDevice?) : Fragment(R.layout.subdialog_add_success) {
    private var name: TextView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById(R.id.add_success_name)
        name?.setText(target?.name)
    }
}