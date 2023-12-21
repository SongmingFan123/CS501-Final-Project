package ch.virt.smartphonemouse.transmission

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// This class stores already known bluetooth devices.
class DeviceStorage(private val context: Context) {
    var devices: MutableList<HostDevice>? = null
    val DEVICES_KEY = "devices"
    init {
        load()
    }

    fun load() {
        val src = PreferenceManager.getDefaultSharedPreferences(
            context
        ).getString(DEVICES_KEY, "[]")
        devices = Gson().fromJson(src, object : TypeToken<ArrayList<HostDevice?>?>() {}.type)
    }

    fun save() {
        val src = Gson().toJson(devices)
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICES_KEY, src)
            .apply()
    }

    fun getDevice(i: Int): HostDevice {
        return devices!![i]
    }

    fun getDevice(address: String?): HostDevice? {
        for (device in devices!!) {
            if (device.address == address) return device
        }
        return null
    }

    fun addDevice(device: HostDevice) {
        devices!!.add(device)
        save()
    }

    fun removeDevice(address: String?) {
        val device = getDevice(address)
        device?.let { removeDevice(it) }
    }

    fun removeDevice(device: HostDevice) {
        devices!!.remove(device)
        save()
    }

    fun hasDevice(address: String?): Boolean {
        return getDevice(address) != null
    }
}