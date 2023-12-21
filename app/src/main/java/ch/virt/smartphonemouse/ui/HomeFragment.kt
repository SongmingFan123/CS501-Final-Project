package ch.virt.smartphonemouse.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.MainActivity
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.ui.home.HomeConnectedSubfragment
import ch.virt.smartphonemouse.ui.home.HomeDisabledSubfragment
import ch.virt.smartphonemouse.ui.home.HomeDisconnectedSubfragment

/**
 * This fragment contains the home page of the app, which shows basic information.
 */
class HomeFragment
/**
 * Creates the fragment.
 *
 * @param bluetooth bluetooth handler to use
 */(private val bluetooth: BluetoothHandler?) :
    Fragment(R.layout.fragment_home) {
    private var status: ImageView? = null
    private var statusText: TextView? = null
    private var debugStatus: TextView? = null
    private var button: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        status = view.findViewById(R.id.home_status)
        statusText = view.findViewById(R.id.home_status_text)
        button = view.findViewById(R.id.home_button)
        debugStatus = view.findViewById(R.id.home_debug_status)
        debugStatus?.setOnClickListener(View.OnClickListener { v: View? -> update() })
        update()
    }

    /**
     * Updates the content of the page according to the current status.
     */
    fun update() {
        if (bluetooth!!.isInitialized) if (!bluetooth.isEnabled) setStatus(
            R.color.status_init,
            R.string.home_status_disabled,
            R.string.home_button_disabled,
            { v: View? -> bluetooth.enableBluetooth() },
            HomeDisabledSubfragment(
                bluetooth
            )
        )
        else if (bluetooth.isConnected) setStatus(
            R.color.status_connected,
            R.string.home_status_connected,
            R.string.home_button_connected,
            { v: View? -> (activity as MainActivity?)!!.navigate(R.id.drawer_mouse) },
            HomeConnectedSubfragment(
                bluetooth
            )
        ) else setStatus(
            R.color.status_disconnected,
            R.string.home_status_disconnected,
            R.string.home_button_disconnected,
            { v: View? -> (activity as MainActivity?)!!.navigate(R.id.drawer_connect) },
            HomeDisconnectedSubfragment()
        )
    }

    /**
     * Sets the status of the page.
     *
     * @param statusColor    color of the status
     * @param statusText     name of the status
     * @param buttonText     text of the primary button of this status
     * @param buttonListener action of the primary button of this status
     * @param fragment       fragment to be displayed
     */
    private fun setStatus(
        statusColor: Int,
        statusText: Int,
        buttonText: Int,
        buttonListener: View.OnClickListener,
        fragment: Fragment
    ) {
        (status!!.background as GradientDrawable).setColor(resources.getColor(statusColor, null))
        this.statusText!!.setText(statusText)
        button!!.isEnabled = true
        button!!.setText(buttonText)
        button!!.setOnClickListener(buttonListener)
        childFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.home_container, fragment).commit()
    }

}