package ch.virt.smartphonemouse.ui.connect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.MainActivity
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler

// This class is a sub fragment for the connect page.
private const val TAG = "ConnectedFragment"
class ConnectConnectedSubfragment (private val bluetooth: BluetoothHandler?) :
    Fragment(R.layout.subfragment_connect_connected) {
    private var elapsed: Chronometer? = null
    private var name: TextView? = null
    private var disconnect: Button? = null
    private var mouse: Button? = null
    private var touchpad: Button? = null
    private var slide: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        elapsed = view.findViewById(R.id.connect_connected_device_elapsed)
        name = view.findViewById(R.id.connect_connected_device_name)
        disconnect = view.findViewById(R.id.connect_connected_disconnect)
        mouse = view.findViewById(R.id.connect_connected_mouse)
        touchpad = view.findViewById(R.id.connect_connected_touchpad)
        slide = view.findViewById(R.id.connect_connected_slide)
        if (bluetooth!!.isConnected) {
            name?.setText(bluetooth.host!!.device!!.name)
            bluetooth.host?.let { elapsed?.setBase(it.connectedSince) }
            elapsed?.setFormat(resources.getString(R.string.connect_connected_elapsed))
            elapsed?.start()
            disconnect?.setOnClickListener(View.OnClickListener { v: View? -> bluetooth.host?.disconnect() })
            mouse?.setOnClickListener(View.OnClickListener { v: View? ->
                (activity as MainActivity?)!!.navigate(
                    R.id.drawer_mouse
                )
            })
            touchpad?.setOnClickListener(View.OnClickListener { v: View? ->
                (activity as MainActivity?)!!.navigate(
                    R.id.drawer_touchpad
                )
            })
            slide?.setOnClickListener(View.OnClickListener { v: View? ->
                (activity as MainActivity?)!!.navigate(
                    R.id.drawer_slides_controller
                )
            })
        }
    }
}