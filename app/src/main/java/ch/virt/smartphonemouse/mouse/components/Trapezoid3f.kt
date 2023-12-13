package ch.virt.smartphonemouse.mouse.components

import ch.virt.smartphonemouse.mouse.math.Vec3f

/**
 * This class is used to calculate a number of following trapezoids. Here with a Vec3 as a value
 */
class Trapezoid3f {
    private var last = Vec3f()

    /**
     * Calculates the current trapezoid
     * @param delta delta time to the last value
     * @param next current value
     * @return trapezoid
     */
    fun trapezoid(delta: Float, next: Vec3f): Vec3f? {
        val result = last.mean(next).multiply(delta)
        last = next
        return result
    }

    /**
     * Resets the last value
     */
    fun reset() {
        last = Vec3f()
    }
}