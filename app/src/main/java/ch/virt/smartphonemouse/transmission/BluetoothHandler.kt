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

private const val TAG = "BluetoothHandler"
// This class handles the bluetooth.
class BluetoothHandler(private val main: ComponentActivity) : ServiceListener {
    var adapter: BluetoothAdapter? = null
    var service: BluetoothHidDevice? = null
    val enableBluetoothLauncher: ActivityResultLauncher<Intent>
    val devices: DeviceStorage
    var isEnabled = false
    var host: HidDevice? = null
    var isInitialized = false
    var discoverer: BluetoothDiscoverer

    init {
        discoverer = BluetoothDiscoverer(main, adapter)
        devices = DeviceStorage(main)
        enableBluetoothLauncher =
            main.registerForActivityResult(StartActivityForResult()) { result: ActivityResult? -> reInit() }
    }

    // Checks whether bluetooth is still turned on
    fun reInitForce(): Boolean {
        if (!adapter!!.isEnabled) {
            reInit()
            return true
        }
        return false
    }

    // Reinitializes the bluetooth things.
    fun reInit() {
        this.isInitialized = false
        init()
    }

    fun enableBluetooth() {
        if (!adapter!!.isEnabled) {
            Log.i(TAG, "Enabling Bluetooth")
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun init() {
        isEnabled = false
        this.isInitialized = false
        adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Log.i(TAG, "Bluetooth is not supported")
            this.isInitialized = true
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
        openProfile()
    }

    // Opens the bluetooth hid profile.
    private fun openProfile() {
        if (!adapter!!.getProfileProxy(main, this, BluetoothProfile.HID_DEVICE)) {
            Log.i(TAG, "Bluetooth HID profile is not supported")
            this.isInitialized = true
            (main as MainActivity).updateBluetoothStatus()
        }
        return
    }

    // Registers the app as a hid device.
    private fun registerApp() {
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
            registerApp()
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            Log.i(TAG, "Reconnecting to Service")
            Toast.makeText(main, "Reloading Bluetooth", Toast.LENGTH_SHORT).show()
            openProfile()
        }
    }

    val isConnected: Boolean
        get() = if (!this.isInitialized) false else host!!.isConnected
    val isConnecting: Boolean
        get() = if (!this.isInitialized) false else host!!.isConnecting

    // Gets a real bluetooth device from a host device that can be saved.
    fun getHostDevice(device: HostDevice): BluetoothDevice? {
        return adapter?.getRemoteDevice(device.address)
    }

    // Returns whether the smartphone is already bonded to a certain device.
    fun isBonded(address: String?): Boolean {
        for (device in adapter!!.bondedDevices) {
            if (device.address == address) return true
        }
        return false
    }
}