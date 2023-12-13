package ch.virt.smartphonemouse.mouse.math

/**
 * This class represents a three-dimensional vector, based on floats
 */
class Vec3f {
    var x = 0f
    var y = 0f
    var z = 0f

    constructor()
    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Returns the x and y axes as a 2d vector
     */
    fun xy(): Vec2f {
        return Vec2f(x, y)
    }

    /**
     * Returns the y and z axes as a 2d vector
     */
    fun yz(): Vec2f {
        return Vec2f(y, z)
    }

    /**
     * Returns the x and z axes as a 2d vector
     */
    fun xz(): Vec2f {
        return Vec2f(x, z)
    }

    /**
     * Returns the absolute value or length of the vector
     */
    fun abs(): Float {
        return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    /**
     * Creates a copy of this vector
     */
    fun copy(): Vec3f {
        return Vec3f(x, y, z)
    }

    /**
     * Adds another vector to this one
     */
    fun add(other: Vec3f?): Vec3f {
        x += other!!.x
        y += other.y
        z += other.z
        return this
    }

    /**
     * Adds another vector to this one
     */
    fun subtract(other: Vec3f): Vec3f {
        x -= other.x
        y -= other.y
        z -= other.z
        return this
    }

    /**
     * Multiplies or scales this vector by an amount
     */
    fun multiply(factor: Float): Vec3f {
        x *= factor
        y *= factor
        z *= factor
        return this
    }

    /**
     * Divides this vector by a given amount
     */
    fun divide(divisor: Float): Vec3f {
        x /= divisor
        y /= divisor
        z /= divisor
        return this
    }

    /**
     * Negates this vector
     */
    fun negative(): Vec3f {
        return multiply(-1f)
    }

    /**
     * Calculates the average between this and a given other vector. Not affecting this instance.
     */
    fun mean(second: Vec3f?): Vec3f {
        return copy().add(second).divide(2f)
    }

    /**
     * Rotates this vector by all three axis by using euler angles in a xyz fashion
     * @param rotation vector containing the rotation for each axis as radians
     */
    fun rotate(rotation: Vec3f?): Vec3f {

        // Calculate sines and cosines that are used (for optimization)
        val sa = Math.sin(rotation!!.x.toDouble()).toFloat()
        val ca = Math.cos(rotation.x.toDouble()).toFloat()
        val sb = Math.sin(rotation.y.toDouble()).toFloat()
        val cb = Math.cos(rotation.y.toDouble()).toFloat()
        val sc = Math.sin(rotation.z.toDouble()).toFloat()
        val cc = Math.cos(rotation.z.toDouble()).toFloat()

        // Apply the rotation (matrix used: xyz)
        val newX = cb * cc * x + cb * -sc * y + sb * z
        val newY = (-sa * -sb * cb + ca * sc) * x + (-sa * -sb * -sc + ca * cc) * y + -sa * cb * z
        val newZ = (ca * -sb * cc + sa * sc) * x + (ca * -sb * -sc + sa * cc) * y + ca * cb * z
        x = newX
        y = newY
        z = newZ
        return this
    }
}