package ch.virt.smartphonemouse.transmission

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import ch.virt.smartphonemouse.MainActivity
import ch.virt.smartphonemouse.transmission.hid.HidDevice


/**
 * This class handles everything related to bluetooth.
 */
class BluetoothHandler(private val main: ComponentActivity) : ServiceListener {
    private var adapter: BluetoothAdapter? = null
    private var service: BluetoothHidDevice? = null

    /**
     * Returns the class responsible for discovering new bluetooth devices.
     *
     * @return bluetooth discoverer
     */
    var discoverer: BluetoothDiscoverer
        private set

    /**
     * Returns a storage of all known devices.
     *
     * @return known devices
     */
    val devices: DeviceStorage

    /**
     * Returns the hid interface.
     *
     * @return hid device to interact with the hid profile
     */
    var host: HidDevice? = null
        private set

    /**
     * Returns whether bluetooth has ben initialized.
     *
     * @return is initialized
     */
    var isInitialized = false
        private set

    /**
     * Returns whether bluetooth is enabled
     *
     * @return is enabled
     */
    var isEnabled = false
        private set

    /**
     * Returns whether the bluetooth hid profile is supported by this device.
     *
     * @return is supported
     */
    var isSupported = false
        private set
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>

//    fun getDiscoverer(): BluetoothDiscoverer? {
//        return discoverer
//    }

    /**
     * Returns a storage of all known devices.
     *
     * @return known devices
     */
//    fun getDevices(): DeviceStorage? {
//        return devices
//    }

    /**
     * Returns the hid interface.
     *
     * @return hid device to interact with the hid profile
     */
//    fun getHost(): HidDevice? {
//        return host
//    }

    /**
     * Creates a bluetooth handler
     *
     * @param context activity to use for various things
     */
    init {
        discoverer = BluetoothDiscoverer(main, adapter)
        devices = DeviceStorage(main)
        enableBluetoothLauncher =
            main.registerForActivityResult(StartActivityForResult()) { result: ActivityResult? -> reInit() }
    }

    /**
     * Checks whether bluetooth is still turned on and if not, reinitializes
     *
     * @return whether a reinitialization is required
     */
    fun reInitRequired(): Boolean {
        if (!adapter!!.isEnabled) {
            reInit()
            return true
        }
        return false
    }

    /**
     * Reinitializes the bluetooth things.
     */
    fun reInit() {
        this.isInitialized = false
        init()
    }

    /**
     * Enables bluetooth by prompting the user.
     */
    fun enableBluetooth() {
        if (!adapter!!.isEnabled) {
            Log.i(TAG, "Enabling Bluetooth")
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    /**
     * Intitializes the bluetooth things.
     */
    private fun init() {
        isSupported = false
        isEnabled = false
        this.isInitialized = false
        adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Log.i(TAG, "Bluetooth is not supported")
            this.isInitialized = true
            isSupported = false
            (main as MainActivity).updateBluetoothStatus()
            return
        }
        if (!adapter!!.isEnabled) {
            Log.i(TAG, "Bluetooth is turned off")
            isEnabled = false
            this.isInitialized = true
            (main as MainActivity).updateBluetoothStatus()
            return
        } else isEnabled = true
        open()
    }

    /**
     * Opens the bluetooth hid profile.
     */
    private fun open() {
        if (!adapter!!.getProfileProxy(main, this, BluetoothProfile.HID_DEVICE)) {
            Log.i(TAG, "Bluetooth HID profile is not supported")
            this.isInitialized = true
            isSupported = false
            (main as MainActivity).updateBluetoothStatus()
            return
        }
        isSupported = true
    }

    /**
     * Registers the app as a hid device.
     */
    private fun start() {
        Log.i(TAG, "Opened HID Profile successfully")
        this.isInitialized = true
        discoverer = BluetoothDiscoverer(main, adapter)
        host = HidDevice(service, this, main)
        Log.d(TAG, "Registering with a HID Device!")
        host!!.register()
        (main as MainActivity).updateBluetoothStatus()
    }

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            service = proxy as BluetoothHidDevice
            start()
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            Log.i(TAG, "Reconnecting to Service")
            Toast.makeText(main, "Reloading Bluetooth", Toast.LENGTH_SHORT).show()
            open()
        }
    }

    val isConnected: Boolean
        /**
         * Returns whether the app is connected to a host device.
         *
         * @return is connected
         */
        get() = if (!this.isInitialized || !isSupported) false else host!!.isConnected
    val isConnecting: Boolean
        /**
         * Returns whether the app is currently connecting to a host device
         *
         * @return is connecting
         */
        get() = if (!this.isInitialized || !isSupported) false else host!!.isConnecting

    /**
     * Gets a real bluetooth device from a host device that can be saved.
     *
     * @param device saved host device
     * @return fetched bluetooth device
     */
    fun fromHostDevice(device: HostDevice): BluetoothDevice? {
        return adapter?.getRemoteDevice(device.address)
    }

    /**
     * Returns whether the smartphone is already bonded to a certain device.
     *
     * @param address address of that device
     * @return whether it is bonded
     */
    fun isBonded(address: String?): Boolean {
//        if (androidx.core.app.ActivityCompat.checkSelfPermission( main,
//                android.Manifest.permission.BLUETOOTH_CONNECT
//            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
//        return false
        for (device in adapter!!.bondedDevices) {
            if (device.address == address) return true
        }
        return false
    }

    companion object {
        private const val TAG = "BluetoothHandler"
    }
}