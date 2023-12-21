package ch.virt.smartphonemouse.mouse.components

import ch.virt.smartphonemouse.mouse.math.Vec2f

// This class is used to represent a vec2 trapezoid.
class Trapezoid2f {
    private var last: Vec2f? = Vec2f()
    fun trapezoid(delta: Float, next: Vec2f?): Vec2f? {
        val result = last!!.mean(next).multiply(delta)
        last = next
        return result
    }

    fun reset() {
        last = Vec2f()
    }
}