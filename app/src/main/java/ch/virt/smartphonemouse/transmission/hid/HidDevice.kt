package ch.virt.smartphonemouse.transmission.hid

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.virt.smartphonemouse.MainActivity
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.transmission.HostDevice

/**
 * This class is used to interact and use the bluetooth hid profile.
 */
class HidDevice
/**
 * Creates this class.
 *
 * @param device    device from android to interact with
 * @param bluetooth bluetooth handler to perform bluetooth actions
 * @param context   context, presumably the main activity used for refreshing the ui
 */(
    private var service: BluetoothHidDevice?,
    private val bluetooth: BluetoothHandler,
    private val context: Context
) : BluetoothHidDevice.Callback() {
    /**
     * Returns the currently connected host device.
     *
     * @return connected device
     */
    var device: BluetoothDevice? = null
        private set

    /**
     * Returns the timestamp when the host device has connected.
     *
     * @return timestamp in milliseconds since the device booted
     */
    var connectedSince: Long = 0
        private set

    /**
     * Returns whether the app is registered.
     *
     * @return is registered
     */
    var isRegistered = false
        private set

    /**
     * Returns whether the app is connected to a host device.
     *
     * @return is connected
     */
    var isConnected = false
        private set

    /**
     * Returns whether the app is currently connecting to a host device.
     *
     * @return is connecting
     */
    var isConnecting = false
        private set
    private var lastFailed = false

    /**
     * Creates the Service Discovery Protocol records.
     *
     * @return Service Discovery Protocol records
     */
    private fun createSDP(): BluetoothHidDeviceAppSdpSettings {
        return BluetoothHidDeviceAppSdpSettings(
            NAME,
            DESCRIPTION,
            PROVIDER,
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            HidDescriptor.DESCRIPTOR
        )
    }

    /**
     * Registers the app as a hid.
     */
    fun register() {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
        if (!isRegistered)
            Log.d(TAG, "Register Result: " + service!!.registerApp(
                createSDP(),
                null,
                null,
                context.mainExecutor,
                this
            ))
        else Log.d(
            TAG, "The Device is already registered!"
        )
    }

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice, registered: Boolean) {
        isRegistered = registered
        Log.d(TAG, "The hid device is now " + if (registered) "registered" else "NOT registered")
    }

    override fun onConnectionStateChanged(de: BluetoothDevice, state: Int) {
        when (state) {
            BluetoothHidDevice.STATE_CONNECTED -> {
                isConnecting = false
                isConnected = true
                connectedSince = SystemClock.elapsedRealtime()
                bluetooth.devices.getDevice(de.address)?.apply {
                    lastConnected = System.currentTimeMillis()
                }
//                bluetooth.devices.getDevice(device.address).lastConnected =
//                    System.currentTimeMillis()
                bluetooth.devices.save()
                (context as MainActivity).updateBluetoothStatus()
                Log.d(TAG, "HID Host connected!")
            }

            BluetoothHidDevice.STATE_DISCONNECTED -> {
                if (!isConnected) lastFailed = true
                isConnecting = false
                isConnected = false
                (context as MainActivity).updateBluetoothStatus()
                Log.d(TAG, "HID Host disconnected!")
            }

            BluetoothHidDevice.STATE_DISCONNECTING -> {disconnect()}

            else -> super.onConnectionStateChanged(de, state)
        }
    }

    /**
     * Connects as a HID to the provided host device
     *
     * @param deviceH host device to connect to
     */
    fun connect(deviceH: HostDevice) {
        if (bluetooth.reInitRequired()) return
        if (isRegistered && !isConnected && !isConnecting) {
            device = bluetooth.fromHostDevice(deviceH)
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//
//            }
            service?.connect(device)
            isConnecting = true
            (context as MainActivity).updateBluetoothStatus()
        } else Log.d(
            TAG,
            "Cannot connect to host whilst connecting or being connected and must be registered"
        )
    }

    /**
     * Disconnects from the host device.
     */
    fun disconnect() {
        Log.d(TAG, "" + isRegistered + isConnected + isConnecting)
        if (isRegistered && isConnected && !isConnecting) {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//
//            }
            service!!.disconnect(device)
        } else Log.d(TAG, "Cannot connect to host whilst connecting or not being connected")
    }

    /**
     * Sends a report to the host device.
     *
     * @param left   whether the left mouse button is pressed
     * @param middle whether the middle mouse button is pressed
     * @param right  whether the right mouse button is pressed
     * @param wheel  change of position of the mouse wheel
     * @param x      change of x position
     * @param y      change of y position
     */
    fun sendReport(left: Boolean, middle: Boolean, right: Boolean, wheel: Int, x: Int, y: Int) {
        if (!isRegistered || !isConnected || isConnecting) {
            Log.d(TAG, "Cannot send a report to the host when no device is connected successfully!")
            return
        }
        val report = ByteArray(4)
        report[0] =
            ((if (left) 1 else 0) or (if (middle) 4 else 0) or if (right) 2 else 0).toByte() // First bit left, second right, third middle and the rest padding
        report[1] = x.toByte()
        report[2] = y.toByte()
        report[3] = wheel.toByte()
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
        service!!.sendReport(device, 1, report) // id 1 because of the descriptor
    }

    /**
     * Returns whether the last connection to a host device has failed.
     *
     * @return has last failed
     */
    fun hasFailed(): Boolean {
        return lastFailed
    }

    /**
     * Marks the last failed status as read.
     */
    fun markFailedAsRead() {
        lastFailed = false
    }

    companion object {
        private const val TAG = "HidDevice"
        private const val NAME = "Smartphone Mouse"
        private const val DESCRIPTION = "Acceleration based Smartphone Mouse"
        private const val PROVIDER = "Virt"
    }
}