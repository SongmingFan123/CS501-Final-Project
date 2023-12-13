package ch.virt.smartphonemouse.ui.connect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
 * This class is a sub fragment for the connect page.
 * This fragment is displayed when the app has a successful connection.
 */
private const val TAG = "ConnectedFragment"
class ConnectConnectedSubfragment
/**
 * Creates this sub fragment.
 *
 * @param bluetooth bluetooth handler to read status from
 */(private val bluetooth: BluetoothHandler?) : Fragment(R.layout.subfragment_connect_connected) {
    private var elapsed: Chronometer? = null
    private var name: TextView? = null
    private var disconnect: Button? = null
    private var mouse: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        elapsed = view.findViewById(R.id.connect_connected_device_elapsed)
        name = view.findViewById(R.id.connect_connected_device_name)
        disconnect = view.findViewById(R.id.connect_connected_disconnect)
        mouse = view.findViewById(R.id.connect_connected_mouse)
        if (bluetooth!!.isConnected) {
//            if (ActivityCompat.checkSelfPermission(requireContext(),
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                name?.setText(bluetooth.host?.device?.name)
//            }
            name?.setText(bluetooth.host?.device?.name)
            bluetooth.host?.let { elapsed?.setBase(it.connectedSince) }
            elapsed?.setFormat(resources.getString(R.string.connect_connected_elapsed))
            elapsed?.start()
            disconnect?.setOnClickListener(View.OnClickListener { v: View? -> bluetooth.host?.disconnect() })
            mouse?.setOnClickListener(View.OnClickListener { v: View? ->
                (activity as MainActivity?)!!.navigate(
                    R.id.drawer_mouse
                )
            })
        }
    }

//    override fun onStop() {
//        super.onStop()
//        Log.d(TAG, "onstop()")
////        bluetooth?.host?.disconnect()
//    }

}