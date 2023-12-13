package ch.virt.smartphonemouse.transmission

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

/**
 * This class handles the discovery of new bluetooth devices to connect to.
 */
class BluetoothDiscoverer(context: Context, private val adapter: BluetoothAdapter?) :
    BroadcastReceiver() {
    /**
     * Returns whether the listener is still scanning for devices.
     *
     * @return is scanning
     */
    var isScanning = false
        private set
    val devices: MutableList<DiscoveredDevice>
    private var updateListener: UpdateListener? = null
    private var scanListener: ScanListener? = null
    private var ctxReference: WeakReference<Context>? = null

    /**
     * Creates a bluetooth discoverer.
     *
     * @param context context to get events from
     * @param adapter bluetooth adapter to use
     */
    init {
        devices = ArrayList()
        val intents = IntentFilter()
        intents.addAction(BluetoothDevice.ACTION_FOUND)
        intents.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intents.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(this, intents)
        ctxReference = WeakReference(context)
    }

    /**
     * Starts the discovery for new devices.
     */
    fun startDiscovery() {
//        if (ctxReference?.get()?.let {
//                ActivityCompat.checkSelfPermission(
//                    it,
//                    Manifest.permission.BLUETOOTH_SCAN
//                )
//            } == PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
        adapter!!.startDiscovery()
    }

    /**
     * Stops the discovery
     */
    fun stopDiscovery() {
//        if (ctxReference?.get()?.let {
//                ActivityCompat.checkSelfPermission(
//                    it,
//                    Manifest.permission.BLUETOOTH_SCAN
//                )
//            } == PackageManager.PERMISSION_GRANTED
//        )
            adapter!!.cancelDiscovery()
    }

    /**
     * Removes the already found devices.
     */
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

    /**
     * Returns the already found devices.
     *
     * @return list of already found devices
     */
//    fun getDevices(): List<DiscoveredDevice> {
//        return devices
//    }

    /**
     * Sets the update listener.
     * This listener gets called when a new device is discovered.
     *
     * @param updateListener update listener
     */
    fun setUpdateListener(updateListener: UpdateListener?) {
        this.updateListener = updateListener
    }

    /**
     * Sets the scan listener.
     * This listener is called when the scan status changes.
     *
     * @param scanListener scan listener
     */
    fun setScanListener(scanListener: ScanListener?) {
        this.scanListener = scanListener
    }

    /**
     * This subclass contains the basic information for a discovered bluetooth device.
     */
    class DiscoveredDevice
    /**
     * Creates a discovered device from a set of information.
     *
     * @param name       name of the device
     * @param address    bluetooth mac address of the device
     * @param majorClass major device class of the device
     */(
        /**
         * Returns the name of the device.
         *
         * @return name
         */
        val name: String,
        /**
         * Returns the bluetooth mac address of the device.
         *
         * @return bluetooth mac address
         */
        val address: String,
        /**
         * Returns the major device class of the device.
         *
         * @return major device class
         */
        val majorClass: Int
    ) {

        /**
         * Creates a discovered device from a bluetooth device.
         *
         * @param device bluetooth device that is the discovered device
         */
        constructor(device: BluetoothDevice?) : this(
            if (device!!.name == null || device.name == "") "Unknown" else device.name,
            device.address,
            device.bluetoothClass.majorDeviceClass
        )

//        fun getAddress(): String {
//            return address
//        }

//        fun getName(): String {
//            return name
//        }
        override fun equals(obj: Any?): Boolean {
            return obj is DiscoveredDevice && address == obj.address
        }

//        fun getMajorClass(): Int {
//            return majorClass
//        }
    }

    /**
     * This interface is a basic listener for updates on the discovered device list.
     */
    interface UpdateListener {
        /**
         * Device list got updated.
         *
         * @param devices currently discovered devices
         */
        fun update(devices: List<DiscoveredDevice>?)
    }

    /**
     * This interface is a basic listener when the scanning starts / stops
     */
    interface ScanListener {
        /**
         * Scan status got changed.
         *
         * @param scanning whether it is currently scanning
         */
        fun changed(scanning: Boolean)
    }
}