package ch.virt.smartphonemouse.customization

import android.content.SharedPreferences

//restoration the setting to default
object DefaultSettings {

    //initializing to default setting depending the preferences previous population record
    fun check(preferences: SharedPreferences) {
        if (!preferences.getBoolean("populated", false)) set(preferences)
    }


    //overwriting settings to default value
    fun set(preferences: SharedPreferences) {
        val edit = preferences.edit()
        edit.putBoolean("populated", true)
        edit.putBoolean("showUsage", true)
        edit.putBoolean("advanced", false)
        edit.apply()
        defaultDebug(preferences)
        defaultInterface(preferences)
        defaultCommunication(preferences)
    }

    private fun defaultDebug(preferences: SharedPreferences) {
        preferences.edit()
            .putBoolean("debugEnabled", false)
            .putString("debugHost", "undefined")
            .putInt("debugPort", 55555)
            .apply()
    }


    //write default interface setting
    private fun defaultInterface(preferences: SharedPreferences) {
        val edit = preferences.edit()
        edit.putString("interfaceTheme", "dark")
        edit.putInt("interfaceBehaviourScrollStep", 50)
        edit.putInt("interfaceBehaviourSpecialWait", 300)
        edit.putBoolean("interfaceVisualsEnable", true)
        edit.putInt("interfaceVisualsStrokeWeight", 4)
        edit.putFloat("interfaceVisualsIntensity", 0.5f)
        edit.putBoolean("interfaceVibrationsEnable", true)
        edit.putInt("interfaceVibrationsButtonIntensity", 50)
        edit.putInt("interfaceVibrationsButtonLength", 30)
        edit.putInt("interfaceVibrationsScrollIntensity", 25)
        edit.putInt("interfaceVibrationsScrollLength", 20)
        edit.putInt("interfaceVibrationsSpecialIntensity", 50)
        edit.putInt("interfaceVibrationsSpecialLength", 50)
        edit.putFloat("interfaceLayoutHeight", 0.3f)
        edit.putFloat("interfaceLayoutMiddleWidth", 0.2f)
        edit.apply()
    }


    //writing default communication setting
    private fun defaultCommunication(preferences: SharedPreferences) {
        val edit = preferences.edit()
        edit.putInt("communicationTransmissionRate", 100)
        edit.apply()
    }
}