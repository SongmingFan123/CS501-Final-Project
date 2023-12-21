package ch.virt.smartphonemouse.mouse.math

import ch.virt.smartphonemouse.mouse.math.Vec3f

// This class is used to represent a vec3 trapezoid.
class Trapezoid3f {
    private var last = Vec3f()

    fun trapezoid(delta: Float, next: Vec3f): Vec3f? {
        val result = last.mean(next).multiply(delta)
        last = next
        return result
    }

    fun reset() {
        last = Vec3f()
    }
}