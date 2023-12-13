package ch.virt.smartphonemouse.ui.connect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler

/**
 * This is a sub fragment for the connect page.
 * This fragment is shown when a recent connection failed.
 */
class ConnectFailedSubfragment
/**
 * Creates the sub fragment.
 * @param bluetooth bluetooth handler to query information from
 */(private val bluetooth: BluetoothHandler?) : Fragment(R.layout.subfragment_connect_failed) {
    private var device: TextView? = null
    private var back: Button? = null
    private var remove: Button? = null
    private var listener: ReturnListener? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        device = view.findViewById(R.id.connect_failed_device)
        back = view.findViewById(R.id.connect_failed_back)
        remove = view.findViewById(R.id.connect_failed_remove)
//        if (ActivityCompat.checkSelfPermission(requireContext(),
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            device?.setText(bluetooth?.host?.device?.name)
//        }
        device?.setText(bluetooth?.host?.device?.name)
        remove?.setOnClickListener(View.OnClickListener { v: View? ->
            bluetooth?.devices?.removeDevice(bluetooth.host?.device?.address)
            bluetooth?.host?.markFailedAsRead()
            listener!!.returned()
        })
        back?.setOnClickListener(View.OnClickListener { v: View? ->
            bluetooth?.host?.markFailedAsRead()
            listener!!.returned()
        })
    }

    /**
     * Sets the return listener that is called when the user wants to return from this fragment
     * @param listener return listener
     */
    fun setReturnListener(listener: ReturnListener?) {
        this.listener = listener
    }

    /**
     * This interface is a basic listener that is called when the user returns from the failure message.
     */
    interface ReturnListener {
        /**
         * Called when the user returns.
         */
        fun returned()
    }
}