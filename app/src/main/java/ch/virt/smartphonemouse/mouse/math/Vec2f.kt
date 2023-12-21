package ch.virt.smartphonemouse.mouse.math

// This class represents a vector consisting of two floats.
class Vec2f {
    var x = 0f
    var y = 0f

    constructor()
    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun length(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    fun add(other: Vec2f?): Vec2f {
        x += other!!.x
        y += other.y
        return this
    }

    fun subtract(other: Vec2f?): Vec2f {
        x -= other!!.x
        y -= other.y
        return this
    }

    fun multiply(factor: Float): Vec2f {
        x *= factor
        y *= factor
        return this
    }

    fun divide(factor: Float): Vec2f {
        x /= factor
        y /= factor
        return this
    }

    fun mean(second: Vec2f?): Vec2f {
        return Vec2f(x, y).add(second).divide(2f)
    }

    fun rotate(rotation: Float): Vec2f {
        val c = Math.cos(rotation.toDouble())
        val s = Math.sin(rotation.toDouble())
        val newX = c * x + -s * y
        val newY = s * x + c * y
        x = newX.toFloat()
        y = newY.toFloat()
        return this
    }
}