package ch.virt.smartphonemouse.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.ui.connect.ConnectConnectedSubfragment
import ch.virt.smartphonemouse.ui.connect.ConnectConnectingSubfragment
import ch.virt.smartphonemouse.ui.connect.ConnectFailedSubfragment
import ch.virt.smartphonemouse.ui.connect.ConnectSelectSubfragment

/**
 * This fragment allows the user to see the current connection status and to change it.
 */
class ConnectFragment
/**
 * Creates the fragment.
 *
 * @param bluetooth bluetooth handler used for bluetooth operations
 */(private val bluetooth: BluetoothHandler?) : Fragment(R.layout.fragment_connect) {
    private var status: ImageView? = null
    private var statusText: TextView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        status = view.findViewById(R.id.connect_status)
        statusText = view.findViewById(R.id.connect_status_text)
        update()
    }

    /**
     * Updates the content on the page according to the current status.
     */
    fun update() {
        if (bluetooth!!.isInitialized) {
            if (bluetooth.isConnecting) {
                loadFragment(ConnectConnectingSubfragment())
                setStatus(R.color.status_connecting, R.string.connect_status_connecting)
            } else if (!bluetooth.isConnected) {
                if (bluetooth.host?.hasFailed() == true) {
                    val fragment = ConnectFailedSubfragment(bluetooth)
                    fragment.setReturnListener (object : ConnectFailedSubfragment.ReturnListener {
                        override fun returned() {
                            update()
                        }
                    })
                    loadFragment(fragment)
                } else loadFragment(ConnectSelectSubfragment(bluetooth))
                setStatus(R.color.status_disconnected, R.string.connect_status_disconnected)
            } else {
                loadFragment(ConnectConnectedSubfragment(bluetooth))
                setStatus(R.color.status_connected, R.string.connect_status_connected)
            }
        }
    }
//    fun update() {
//        if (bluetooth!!.isInitialized) {
//            if (!bluetooth.isConnected) {
//                loadFragment(ConnectSelectSubfragment(bluetooth))
//                setStatus(R.color.status_disconnected, R.string.connect_status_disconnected)
//            } else {
//                loadFragment(ConnectConnectedSubfragment(bluetooth))
//                setStatus(R.color.status_connected, R.string.connect_status_connected)
//            }
//        }
//    }

    /**
     * Sets the status of the page.
     *
     * @param color color of the current status
     * @param text  name of the current status
     */
    private fun setStatus(color: Int, text: Int) {
        (status!!.background as GradientDrawable).setColor(resources.getColor(color, null))
        statusText!!.setText(text)
    }

    /**
     * Sets the inner content to the requested fragment.
     *
     * @param fragment fragment to set to
     */
    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.connect_container, fragment).commit()
    }
}