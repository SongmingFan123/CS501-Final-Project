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
import ch.virt.smartphonemouse.ui.connect.ConnectSelectSubfragment


 //This fragment to show the user the current connection status and allow the user to change it.

class ConnectFragment

 //Fragment created

    (private val bluetooth: BluetoothHandler?) : Fragment(R.layout.fragment_connect) {
    private var status: ImageView? = null
    private var statusText: TextView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        status = view.findViewById(R.id.connect_status)
        statusText = view.findViewById(R.id.connect_status_text)
        update()
    }


    //Content of page updated depending on the current status

    fun update() {
        if (bluetooth!!.isInitialized) {
            if (bluetooth.isConnecting) {
                childFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.connect_container, ConnectConnectingSubfragment()).commit()
                (status!!.background as GradientDrawable).setColor(resources.getColor(R.color.status_connecting, null))
                statusText!!.setText(R.string.connect_status_connecting)
            } else if (!bluetooth.isConnected) {
                childFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.connect_container, ConnectSelectSubfragment(bluetooth)).commit()
                (status!!.background as GradientDrawable).setColor(resources.getColor(R.color.status_disconnected, null))
                statusText!!.setText(R.string.connect_status_disconnected)
            } else {
                childFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.connect_container, ConnectConnectedSubfragment(bluetooth)).commit()
                (status!!.background as GradientDrawable).setColor(resources.getColor(R.color.status_connected, null))
                statusText!!.setText(R.string.connect_status_connected)
            }
        }
    }

}