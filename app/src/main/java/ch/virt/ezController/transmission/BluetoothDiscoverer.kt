package ch.virt.ezController.transmission

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

// This class handles the discovery of new bluetooth devices to connect to.
class BluetoothDiscoverer(private val context: Context, private val adapter: BluetoothAdapter?) :
    BroadcastReceiver() {

    var isScanning = false
    val devices: MutableList<DiscoveredDevice>
    private var updateListener: UpdateListener? = null
    private var scanListener: ScanListener? = null
    private var ctxReference: WeakReference<Context>? = null

    init {
        devices = ArrayList()
        val intents = IntentFilter()
        intents.addAction(BluetoothDevice.ACTION_FOUND)
        intents.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intents.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(this, intents)
        ctxReference = WeakReference(context)
    }

    // Starts the discovery for new devices
    fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            adapter!!.startDiscovery()
        }

    }

    // Stops the discovery
    fun stopDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            adapter!!.cancelDiscovery()
        }

    }

    // Removes the already found devices.
    fun reset() {
        devices.clear()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (BluetoothDevice.ACTION_FOUND == action) {
            val discovered =
                DiscoveredDevice(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
            if (!devices.contains(discovered)) { // Ignore duplicates
                devices.add(discovered)
                if (updateListener != null) updateListener!!.update(devices)
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action || BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
            isScanning = BluetoothAdapter.ACTION_DISCOVERY_STARTED == action
            if (scanListener != null) scanListener!!.changed(isScanning)
        }
    }

    // Sets the update listener.
    fun setUpdateListener(updateListener: UpdateListener?) {
        this.updateListener = updateListener
    }

    // Sets the scan listener.
    fun setScanListener(scanListener: ScanListener?) {
        this.scanListener = scanListener
    }

    // This subclass contains the basic information for a discovered bluetooth device.
    class DiscoveredDevice
    (
        val name: String,
        val address: String,
        val majorClass: Int
    ) {
        constructor(device: BluetoothDevice?) : this(
            if (device!!.name == null || device.name == "") "Unknown" else device.name,
            device.address,
            device.bluetoothClass.majorDeviceClass
        )
        override fun equals(obj: Any?): Boolean {
            return obj is DiscoveredDevice && address == obj.address
        }
    }
    //update bluetooth listener
    interface UpdateListener {
        fun update(devices: List<DiscoveredDevice>?)
    }
    //scan bluetooth listener
    interface ScanListener {
        fun changed(scanning: Boolean)
    }
}