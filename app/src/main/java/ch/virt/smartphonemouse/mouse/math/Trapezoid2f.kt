package ch.virt.smartphonemouse.mouse.math

import ch.virt.smartphonemouse.mouse.math.Vec2f

// This class is used to represent a vec2 trapezoid.
class Trapezoid2f {
    private var last: Vec2f? = Vec2f()
    //creation of vec2 trapezoid given delta and next vec2 trapezoid
    fun trapezoid(delta: Float, next: Vec2f?): Vec2f? {
        val result = last!!.mean(next).multiply(delta)
        last = next
        return result
    }
    //reset vec2f trapezoid
    fun reset() {
        last = Vec2f()
    }
}