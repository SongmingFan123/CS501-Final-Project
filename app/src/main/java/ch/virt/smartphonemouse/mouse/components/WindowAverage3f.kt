package ch.virt.smartphonemouse.mouse.components

import ch.virt.smartphonemouse.mouse.math.Vec3f

/**
 * This class represents an average which operates in a window, remembering a given value of past samples to do the averaging with.
 * Here with a Vec3f as a value.
 */
class WindowAverage3f(length: Int) {
    private var index = 0
    private var elements: Array<Vec3f?>

    /**
     * @param length length of the window in values
     */
    init {
        elements = arrayOfNulls(length)
    }

    /**
     * Takes the next sample and calculates the current average
     * @param next next sample
     * @return current average, including next sample
     */
    fun avg(next: Vec3f?): Vec3f? {
        elements[index % elements.size] = next
        index++
        val total = Vec3f()
        val amount = Math.min(elements.size, index)
        for (i in 0 until amount) {
            total.add(elements[i])
        }
        return total.divide(amount.toFloat())
    }

    /**
     * Resets the average
     */
    fun reset() {
        elements = arrayOfNulls(elements.size)
        index = 0
    }
}