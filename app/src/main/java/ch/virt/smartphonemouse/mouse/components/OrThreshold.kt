package ch.virt.smartphonemouse.mouse.components

/**
 * This class represents a threshold which operates based on two values.
 * It uses the values in an OR fashion, activating when one or both values surpass their threshold.
 * It also takes a time to drop off, after the last active value pair.
 */
class OrThreshold(
    private val dropoff: Int,
    private val firstThreshold: Float,
    private val secondThreshold: Float
) {
    private var lastActive: Int

    /**
     * @param dropoff amount of samples it takes from the last active sample to be inactive
     * @param firstThreshold threshold the first value has to surpass
     * @param secondThreshold threshold the second value has to surpass
     */
    init {
        lastActive = dropoff
    }

    /**
     * Takes two new values and determines whether the threshold is activated
     * @param first first value
     * @param second second value
     * @return whether the threshold is active
     */
    fun active(first: Float, second: Float): Boolean {
        if (first > firstThreshold || second > secondThreshold) lastActive = 0 else lastActive++
        return lastActive <= dropoff
    }
}