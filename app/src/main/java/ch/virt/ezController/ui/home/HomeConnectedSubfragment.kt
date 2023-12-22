package ch.virt.ezController.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.ezController.MainActivity
import ch.virt.ezController.R
import ch.virt.ezController.transmission.BluetoothHandler

//Sub fragment that gets the home screen displayed while a device is being connected
class HomeConnectedSubfragment
//Creation of the sub fragment.

    (private val handler: BluetoothHandler?) : Fragment(R.layout.subfragment_home_connected) {
    private var chronometer: Chronometer? = null
    private var device: TextView? = null
    private var more: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chronometer = view.findViewById(R.id.home_connected_device_elapsed)
        device = view.findViewById(R.id.home_connected_device_name)
        more = view.findViewById(R.id.home_connected_more)
        handler?.host?.connectedSince?.let { chronometer?.setBase(it) }
        chronometer?.setFormat(resources.getString(R.string.home_connected_elapsed))
        chronometer?.start()
        device?.setText(handler?.host?.device?.name)
        more?.setOnClickListener(View.OnClickListener { v: View? ->
            (activity as MainActivity?)!!.navigate(
                R.id.drawer_connect
            )
        })
    }
}