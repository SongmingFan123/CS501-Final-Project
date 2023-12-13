package ch.virt.smartphonemouse.mouse.math

/**
 * This class represents a vector consisting of two floats.
 */
class Vec2f {
    var x = 0f
    var y = 0f

    constructor()
    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /**
     * Creates a copy of the vector
     */
    fun copy(): Vec2f {
        return Vec2f(x, y)
    }

    /**
     * Returns the absolute value, aka. length of the vector
     */
    fun abs(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * Adds another vector to this one
     */
    fun add(other: Vec2f?): Vec2f {
        x += other!!.x
        y += other.y
        return this
    }

    /**
     * Adds another vector to this one
     */
    fun subtract(other: Vec2f?): Vec2f {
        x -= other!!.x
        y -= other.y
        return this
    }

    /**
     * Multiplies or scales this vector by an amount
     */
    fun multiply(factor: Float): Vec2f {
        x *= factor
        y *= factor
        return this
    }

    /**
     * Divides this vector by a given amount
     */
    fun divide(divisor: Float): Vec2f {
        x /= divisor
        y /= divisor
        return this
    }

    /**
     * Calculates the average between this and a given other vector. Not affecting this instance.
     */
    fun mean(second: Vec2f?): Vec2f {
        return copy().add(second).divide(2f)
    }

    /**
     * Rotates this vector by a given rotation
     * @param rotation rotation in radians
     */
    fun rotate(rotation: Float): Vec2f {
        val c = Math.cos(rotation.toDouble()).toFloat()
        val s = Math.sin(rotation.toDouble()).toFloat()
        val newX = c * x + -s * y
        val newY = s * x + c * y
        x = newX
        y = newY
        return this
    }
}