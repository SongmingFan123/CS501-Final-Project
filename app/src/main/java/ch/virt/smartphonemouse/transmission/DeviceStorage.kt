package ch.virt.smartphonemouse.transmission

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// This class stores known bluetooth devices.
class DeviceStorage(private val context: Context) {
    var devices: MutableList<HostDevice>? = null
    val DEVICES_KEY = "devices"
    init {
        load()
    }
//load stored devices
    fun load() {
        val src = PreferenceManager.getDefaultSharedPreferences(
            context
        ).getString(DEVICES_KEY, "[]")
        devices = Gson().fromJson(src, object : TypeToken<ArrayList<HostDevice?>?>() {}.type)
    }
    //save stored devices
    fun save() {
        val src = Gson().toJson(devices)
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICES_KEY, src)
            .apply()
    }
    //get stored device
    fun getDevice(i: Int): HostDevice {
        return devices!![i]
    }
    //get stored device
    fun getDevice(address: String?): HostDevice? {
        for (device in devices!!) {
            if (device.address == address) return device
        }
        return null
    }
    //add device
    fun addDevice(device: HostDevice) {
        devices!!.add(device)
        save()
    }
    //remove device given address
    fun removeDevice(address: String?) {
        val device = getDevice(address)
        device?.let { removeDevice(it) }
    }
    //remove device based on host device
    fun removeDevice(device: HostDevice) {
        devices!!.remove(device)
        save()
    }
    //if the storage has device
    fun hasDevice(address: String?): Boolean {
        return getDevice(address) != null
    }
}