package ch.virt.smartphonemouse.mouse.math

import ch.virt.smartphonemouse.mouse.math.Vec3f

// This class is used to represent a vec3 trapezoid.
class Trapezoid3f {
    private var last = Vec3f()
    //creation of vec3 trapezoid given delta and next vec3 trapezoid
    fun trapezoid(delta: Float, next: Vec3f): Vec3f? {
        val result = last.mean(next).multiply(delta)
        last = next
        return result
    }
    //reset vec3f trapezoid
    fun reset() {
        last = Vec3f()
    }
}