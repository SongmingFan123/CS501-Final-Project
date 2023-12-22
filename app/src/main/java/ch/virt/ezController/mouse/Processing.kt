package ch.virt.ezController.mouse

import ch.virt.ezController.mouse.math.Trapezoid2f
import ch.virt.ezController.mouse.math.Trapezoid3f
import ch.virt.ezController.mouse.math.WindowAverage
import ch.virt.ezController.mouse.math.WindowAverage3f
import ch.virt.ezController.mouse.math.Vec2f
import ch.virt.ezController.mouse.math.Vec3f
import ch.virt.ezController.transmission.DebugTransmitter

class Processing(private val debug: DebugTransmitter?, private val parameters: Parameters) {
    private val rotationDeltaTrapezoid = Trapezoid3f()
    private val activeGravityAverage: WindowAverage
    private val inactiveGravityAverage: WindowAverage3f
    private var gravityCurrent: Vec3f?
    private val activeNoiseAverage: WindowAverage
    private var lastActive = false
    private val distanceVelocityTrapezoid: Trapezoid2f
    private var distanceVelocity: Vec2f
    private val distanceDistanceTrapezoid: Trapezoid2f
    private val sensitivity: Float
    private val enableGravityRotation: Boolean

    init {
        activeGravityAverage = WindowAverage(parameters.lengthWindowGravity)
        activeNoiseAverage = WindowAverage(parameters.lengthWindowNoise)
        inactiveGravityAverage = WindowAverage3f(parameters.lengthGravity)
        gravityCurrent = Vec3f()
        distanceVelocityTrapezoid = Trapezoid2f()
        distanceDistanceTrapezoid = Trapezoid2f()
        distanceVelocity = Vec2f()
        sensitivity = parameters.sensitivity
        enableGravityRotation = parameters.enableGravityRotation
    }
    //process the distance
    fun step(time: Float, delta: Float, acceleration: Vec3f, angularVelocity: Vec3f): Vec2f? {
        debug!!.stageFloat(time)
        debug.stageVec3f(acceleration)
        debug.stageVec3f(angularVelocity)

        val rotationDelta = rotationDeltaTrapezoid.trapezoid(delta, angularVelocity)
        val active = active(acceleration, angularVelocity)
        debug.stageBoolean(active)
        val linearAcceleration = gravity(active, acceleration, rotationDelta)
        debug.stageVec2f(linearAcceleration)
        val distance = distance(delta, active, linearAcceleration, rotationDelta)
        debug.stageVec2f(distanceVelocity) // Do this here because it did not fit into the method
        debug.stageVec2f(distance)

        lastActive = active
        debug.commit()
        return distance
    }
    //handling activeness based on given acceleration and angular velocity
    fun active(acceleration: Vec3f, angularVelocity: Vec3f): Boolean {

        var acc = acceleration.xy().length()
        debug!!.stageFloat(acc)

        val gravity = activeGravityAverage.avg(acc)
        debug.stageFloat(gravity)
        acc -= gravity
        acc = Math.abs(acc)

        acc = activeNoiseAverage.avg(acc)
        debug.stageFloat(acc)

        val rot = Math.abs(angularVelocity.z)
        debug.stageFloat(rot)

        return acc > parameters.thresholdAcceleration || rot > parameters.thresholdRotation
    }
    //handling gravity
    fun gravity(active: Boolean, acceleration: Vec3f, rotationDelta: Vec3f?): Vec2f? {

        if (active) {
            if (!lastActive) {
                inactiveGravityAverage.reset()
            }
            var tmp = Vec3f(rotationDelta!!.x, rotationDelta.y, rotationDelta.z)
            if (enableGravityRotation) gravityCurrent!!.rotate(tmp.negative())
        } else {
            gravityCurrent = inactiveGravityAverage.avg(acceleration)
        }
        debug!!.stageVec3f(gravityCurrent)

        return acceleration.xy().subtract(gravityCurrent!!.xy())
    }
    //handling distance depending on velocity
    fun distance(
        delta: Float,
        active: Boolean,
        linearAcceleration: Vec2f?,
        rotationDelta: Vec3f?
    ): Vec2f? {
        return if (active) {
            distanceVelocity.rotate(-rotationDelta!!.z)
            distanceVelocity.add(distanceVelocityTrapezoid.trapezoid(delta, linearAcceleration))
            distanceDistanceTrapezoid.trapezoid(delta, distanceVelocity)!!.multiply(sensitivity)
        } else {
            if (lastActive) {
                distanceVelocity = Vec2f()
                distanceVelocityTrapezoid.reset()
                distanceDistanceTrapezoid.reset()
            }
            Vec2f()
        }
    }

    companion object {
        fun registerDebugColumns(debug: DebugTransmitter) {
            debug.registerColumn("time", Float::class.java)
            debug.registerColumn("acceleration", Vec3f::class.java)
            debug.registerColumn("angular-velocity", Vec3f::class.java)
            debug.registerColumn("active-acc-abs", Float::class.java)
            debug.registerColumn("active-acc-grav", Float::class.java)
            debug.registerColumn("active-acc", Float::class.java)
            debug.registerColumn("active-rot", Float::class.java)
            debug.registerColumn("active", Boolean::class.java)
            debug.registerColumn("gravity", Vec3f::class.java)
            debug.registerColumn("acceleration-linear", Vec2f::class.java)
            debug.registerColumn("velocity", Vec2f::class.java)
            debug.registerColumn("distance", Vec2f::class.java)
        }
    }
}