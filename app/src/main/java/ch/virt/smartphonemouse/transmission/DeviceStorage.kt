package ch.virt.smartphonemouse.transmission

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * This class stores already known bluetooth devices.
 */
class DeviceStorage(private val context: Context) {
    var devices: MutableList<HostDevice>? = null

    /**
     * Creates and loads the device storage.
     *
     * @param context context to access preferences
     */
    init {
        load()
    }

    /**
     * Loads the devices from the preferences.
     */
    fun load() {
        val src = PreferenceManager.getDefaultSharedPreferences(
            context
        ).getString(DEVICES_KEY, "[]")
        devices = Gson().fromJson(src, object : TypeToken<ArrayList<HostDevice?>?>() {}.type)
    }

    /**
     * Saves the devices to the preferences.
     */
    fun save() {
        devices!!.sortWith { o1: HostDevice, o2: HostDevice ->
            -java.lang.Long.compare(
                o1.lastConnected,
                o2.lastConnected
            )
        }
        val src = Gson().toJson(devices)
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICES_KEY, src)
            .apply()
    }

    /**
     * Returns all known devices.
     *
     * @return list of known host devices.
     */

    /**
     * Returns a specific host device at a certain index.
     *
     * @param i index of the device.
     * @return known device
     */
    fun getDevice(i: Int): HostDevice {
        return devices!![i]
    }

    /**
     * Returns a specific host device with a certain bluetooth mac address.
     *
     * @param address bluetooth mac address
     * @return known device
     */
    fun getDevice(address: String?): HostDevice? {
        for (device in devices!!) {
            if (device.address == address) return device
        }
        return null
    }

    /**
     * Adds a device to the storage.
     *
     * @param device known device to add
     */
    fun addDevice(device: HostDevice) {
        devices!!.add(device)
        save()
    }

    /**
     * Removes a device at an index from the known devices.
     *
     * @param i index of that device
     */
    fun removeDevice(i: Int) {
        val device = getDevice(i)
        if (device != null) {
            removeDevice(device)
        }
    }

    /**
     * Removes a device with a specific bluetooth mac address.
     *
     * @param address bluetooth mac address of that device
     */
    fun removeDevice(address: String?) {
        val device = getDevice(address)
        device?.let { removeDevice(it) }
    }

    /**
     * Removes a known device.
     *
     * @param device device to remove
     */
    fun removeDevice(device: HostDevice) {
        devices!!.remove(device)
        save()
    }

    /**
     * Returns whether a certain device is present in the known devices.
     *
     * @param address bluetooth mac address of said device
     * @return whether it is present
     */
    fun hasDevice(address: String?): Boolean {
        return getDevice(address) != null
    }

    companion object {
        const val DEVICES_KEY = "devices"
    }
}