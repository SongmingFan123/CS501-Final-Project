package ch.virt.smartphonemouse.mouse.components

import ch.virt.smartphonemouse.mouse.math.Vec2f

/**
 * This class is used to calculate a number of following trapezoids. Here with a Vec2 as a value
 */
class Trapezoid2f {
    private var last: Vec2f? = Vec2f()

    /**
     * Calculates the current trapezoid
     * @param delta delta time to the last value
     * @param next current value
     * @return trapezoid
     */
    fun trapezoid(delta: Float, next: Vec2f?): Vec2f? {
        val result = last!!.mean(next).multiply(delta)
        last = next
        return result
    }

    /**
     * Resets the last value
     */
    fun reset() {
        last = Vec2f()
    }
}