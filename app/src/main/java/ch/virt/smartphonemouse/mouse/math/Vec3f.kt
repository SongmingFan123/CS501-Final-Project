package ch.virt.smartphonemouse.mouse.math

// This class represents a vector consisting of three floats.
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
    //creation vec2f given x and y indexes
    fun xy(): Vec2f {
        return Vec2f(x, y)
    }
    //add vec3f
    fun add(other: Vec3f?): Vec3f {
        x += other!!.x
        y += other.y
        z += other.z
        return this
    }
    //multiplication of vec3f by a factor
    fun multiply(factor: Float): Vec3f {
        x *= factor
        y *= factor
        z *= factor
        return this
    }
    //division of vec3f by a factor
    fun divide(divisor: Float): Vec3f {
        x /= divisor
        y /= divisor
        z /= divisor
        return this
    }
    //convert vec3f to a negative instance
    fun negative(): Vec3f {
        return multiply(-1f)
    }
    //calculation of mean of vec3f by a factor
    fun mean(second: Vec3f?): Vec3f {
        return Vec3f(x, y, z).add(second).divide(2f)
    }
    //rotation of vec3f
    fun rotate(rotation: Vec3f?): Vec3f {

        val sa = Math.sin(rotation!!.x.toDouble()).toFloat()
        val ca = Math.cos(rotation.x.toDouble()).toFloat()
        val sb = Math.sin(rotation.y.toDouble()).toFloat()
        val cb = Math.cos(rotation.y.toDouble()).toFloat()
        val sc = Math.sin(rotation.z.toDouble()).toFloat()
        val cc = Math.cos(rotation.z.toDouble()).toFloat()

        val newX = cb * cc * x + cb * -sc * y + sb * z
        val newY = (-sa * -sb * cb + ca * sc) * x + (-sa * -sb * -sc + ca * cc) * y + -sa * cb * z
        val newZ = (ca * -sb * cc + sa * sc) * x + (ca * -sb * -sc + sa * cc) * y + ca * cb * z
        x = newX
        y = newY
        z = newZ
        return this
    }
}