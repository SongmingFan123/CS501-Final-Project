package ch.virt.smartphonemouse.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
private const val TAG = "Home Fragment"
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        status = view.findViewById(R.id.home_status)
        statusText = view.findViewById(R.id.home_status_text)
        button = view.findViewById(R.id.home_button)
        debugStatus = view.findViewById(R.id.home_debug_status)
        debugStatus?.setOnClickListener(View.OnClickListener { v: View? -> update() })
        update()
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {}
        else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
        }

    /**
     * Updating content of the page depending on current page status.
     */
    fun update() {
        if (bluetooth!!.isInitialized) if (!bluetooth.isEnabled){
            setStatus(
                R.color.status_init,
                R.string.home_status_disabled,
                R.string.home_button_disabled,
                { v: View? -> bluetooth.enableBluetooth() },
                HomeDisabledSubfragment(
                    bluetooth
                )
            )
        }
        else if (bluetooth.isConnected){
            setStatus(
                R.color.status_connected,
                R.string.home_status_connected,
                R.string.home_button_connected,
                { v: View? -> (activity as MainActivity?)!!.navigate(R.id.drawer_mouse) },
                HomeConnectedSubfragment(
                    bluetooth
                )
            )
        }  else{
            setStatus(
                R.color.status_disconnected,
                R.string.home_status_disconnected,
                R.string.home_button_disconnected,
                { v: View? -> (activity as MainActivity?)!!.navigate(R.id.drawer_connect) },
                HomeDisconnectedSubfragment()
            )
        }
    }

    /**
     * Setting page status
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