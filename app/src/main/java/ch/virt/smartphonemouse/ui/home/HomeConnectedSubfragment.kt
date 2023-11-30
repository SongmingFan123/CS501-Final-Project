package ch.virt.smartphonemouse.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.MainActivity
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler

/**
 * This sub fragment gets shown on the home screen when a device is currently connected.
 */
class HomeConnectedSubfragment
/**
 * Creates the sub fragment.
 *
 * @param handler bluetooth handler to get connected information from
 */(private val handler: BluetoothHandler?) : Fragment(R.layout.subfragment_home_connected) {
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            device?.setText(handler?.host?.device?.name)
        }
        more?.setOnClickListener(View.OnClickListener { v: View? ->
            (activity as MainActivity?)!!.navigate(
                R.id.drawer_connect
            )
        })
    }
}