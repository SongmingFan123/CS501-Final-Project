package ch.virt.smartphonemouse.transmission

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.os.SystemClock
import android.util.Log
import ch.virt.smartphonemouse.MainActivity

private const val TAG = "HidDevice"
// This class is used to interact and use the bluetooth hid profile.
class HidDevice
(
    private var service: BluetoothHidDevice?,
    private val bluetooth: BluetoothHandler,
    private val context: Context
) : BluetoothHidDevice.Callback() {
    private var isRegistered = false
    var isConnected = false
    var isConnecting = false
    var device: BluetoothDevice? = null
    var connectedSince: Long = 0

    // Creates the Service Discovery Protocol records.
    private fun createSDP(): BluetoothHidDeviceAppSdpSettings {
        return BluetoothHidDeviceAppSdpSettings(
            "EzController",
            "DESCRIPTION",
            "PROVIDER",
            BluetoothHidDevice.SUBCLASS1_MOUSE,
            HidDescriptor.DESCRIPTOR
        )
    }

    // Registers the app as a hid.
    fun register() {
        if (!isRegistered)
            Log.d(
                TAG,
                "Register Result: " + service!!.registerApp(
                    createSDP(),
                    null,
                    null,
                    context.mainExecutor,
                    this
                )
            )
        else Log.d(
            TAG, "The Device is already registered!"
        )
    }

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice, registered: Boolean) {
        isRegistered = registered
        Log.d(
            TAG,
            "The hid device is now " + if (registered) "registered" else "NOT registered"
        )
    }

    override fun onConnectionStateChanged(de: BluetoothDevice, state: Int) {
        when (state) {
            BluetoothHidDevice.STATE_CONNECTED -> {
                isConnecting = false
                isConnected = true
                connectedSince = SystemClock.elapsedRealtime()
                bluetooth.devices.save()
                (context as MainActivity).updateBluetoothStatus()
                Log.d(TAG, "HID Host connected!")
            }

            BluetoothHidDevice.STATE_DISCONNECTED -> {
                isConnecting = false
                isConnected = false
                (context as MainActivity).updateBluetoothStatus()
                Log.d(TAG, "HID Host disconnected!")
            }

            BluetoothHidDevice.STATE_DISCONNECTING -> {disconnect()}

            else -> super.onConnectionStateChanged(de, state)
        }
    }

    // Connects as a HID to the provided host device
    fun connect(deviceH: HostDevice) {
        if (bluetooth.reInitForce()) return
        if (isRegistered && !isConnected && !isConnecting) {
            device = bluetooth.getHostDevice(deviceH)
            service!!.connect(device)
            isConnecting = true
            (context as MainActivity).updateBluetoothStatus()
        } else Log.d(
            TAG,
            "Cannot connect to host whilst connecting or being connected and must be registered"
        )
    }

    // Disconnects from the host device.
    fun disconnect() {
        Log.d(
            TAG,
            "" + isRegistered + isConnected + isConnecting
        )
        if (isRegistered && isConnected && !isConnecting) {
            service!!.disconnect(device)
        } else Log.d(
            TAG,
            "Cannot connect to host whilst connecting or not being connected"
        )
    }

    // Sends a report to the host device.
    fun sendReport(left: Boolean, middle: Boolean, right: Boolean, wheel: Int, x: Int, y: Int) {
        if (!isRegistered || !isConnected || isConnecting) {
            Log.d(
                TAG,
                "Cannot send a report to the host when no device is connected successfully!"
            )
            return
        }
        val report = ByteArray(4)
        report[0] =
            ((if (left) 1 else 0) or (if (middle) 4 else 0) or if (right) 2 else 0).toByte() // First bit left, second right, third middle and the rest padding
        report[1] = x.toByte()
        report[2] = y.toByte()
        report[3] = wheel.toByte()
        service!!.sendReport(device, 1, report)
    }
}