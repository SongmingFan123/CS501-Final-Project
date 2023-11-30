package ch.virt.smartphonemouse.transmission

/**
 * This class represents a known host device.
 */
class HostDevice(
    /**
     * Returns the bluetooth mac address of the device.
     *
     * @return bluetooth mac address
     */
    val address: String?,
    /**
     * Returns the name of the device.
     *
     * @return name
     */
    val name: String?
) {
    /**
     * Returns when the device was last connected to.
     *
     * @return unix timestamp of when it was last connected
     */
    /**
     * Sets when the device has last connected.
     *
     * @param lastConnected unix timestamp when last connected
     */
    var lastConnected: Long

    /**
     * Creates a host device.
     *
     * @param address bluetooth mac address of the device
     * @param name    name of the device
     */
    init {
        lastConnected = -1
    }
}