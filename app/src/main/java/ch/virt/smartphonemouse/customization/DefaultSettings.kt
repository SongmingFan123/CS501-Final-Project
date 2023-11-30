package ch.virt.smartphonemouse.customization

import android.content.SharedPreferences

/**
 * This class is used for restoring the settings to their factory defaults.
 */
object DefaultSettings {
    /**
     * Checks whether the preferences have once been populated.
     * If not, they get initialized to the default settings.
     *
     * @param preferences preferences to check in
     */
    fun check(preferences: SharedPreferences) {
        if (!preferences.getBoolean("populated", false)) set(preferences)
    }

    /**
     * Overwrites the settings to the defaults
     *
     * @param preferences preferences to write in
     */
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

    /**
     * Writes the default interface settings
     *
     * @param preferences preferences to write in
     */
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


    /**
     * Writes the default communication settings
     *
     * @param preferences preferences to write in
     */
    private fun defaultCommunication(preferences: SharedPreferences) {
        val edit = preferences.edit()
        edit.putInt("communicationTransmissionRate", 100)
        edit.apply()
    }
}