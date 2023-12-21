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
    //length of vec2f trapezoid
    fun length(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
    //add given vec2f
    fun add(other: Vec2f?): Vec2f {
        x += other!!.x
        y += other.y
        return this
    }
    //subtract given vec2f
    fun subtract(other: Vec2f?): Vec2f {
        x -= other!!.x
        y -= other.y
        return this
    }
    //multiply by given vec2f
    fun multiply(factor: Float): Vec2f {
        x *= factor
        y *= factor
        return this
    }
    //divide by given vec2f
    fun divide(factor: Float): Vec2f {
        x /= factor
        y /= factor
        return this
    }
    //mean of given vec2f
    fun mean(second: Vec2f?): Vec2f {
        return Vec2f(x, y).add(second).divide(2f)
    }
    //rotate by given rotation
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