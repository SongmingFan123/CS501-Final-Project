package ch.virt.smartphonemouse.mouse.components

/**
 * This class represents an average which operates in a window, remembering a given value of past samples to do the averaging with.
 */
class WindowAverage(length: Int) {
    private var index = 0
    private var elements: FloatArray

    /**
     * @param length length of the window in values
     */
    init {
        elements = FloatArray(length)
    }

    /**
     * Takes the next sample and calculates the current average
     * @param next next sample
     * @return current average, including next sample
     */
    fun avg(next: Float): Float {
        elements[index % elements.size] = next
        index++
        var total = 0f
        val amount = Math.min(elements.size, index)
        for (i in 0 until amount) {
            total += elements[i]
        }
        return total / amount
    }

    /**
     * Resets the average
     */
    fun reset() {
        elements = FloatArray(elements.size)
        index = 0
    }
}