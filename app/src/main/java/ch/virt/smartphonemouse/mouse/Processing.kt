package ch.virt.smartphonemouse.mouse

import ch.virt.smartphonemouse.mouse.components.OrThreshold
import ch.virt.smartphonemouse.mouse.components.Trapezoid2f
import ch.virt.smartphonemouse.mouse.components.Trapezoid3f
import ch.virt.smartphonemouse.mouse.components.WindowAverage
import ch.virt.smartphonemouse.mouse.components.WindowAverage3f
import ch.virt.smartphonemouse.mouse.math.Vec2f
import ch.virt.smartphonemouse.mouse.math.Vec3f
import ch.virt.smartphonemouse.transmission.DebugTransmitter

class Processing(private val debug: DebugTransmitter?, parameters: Parameters) {
    private val rotationDeltaTrapezoid: Trapezoid3f
    private val activeGravityAverage: WindowAverage
    private val activeNoiseAverage: WindowAverage
    private val activeThreshold: OrThreshold
    private var lastActive = false
    private val gravityInactiveAverage: WindowAverage3f
    private var gravityCurrent: Vec3f?
    private val distanceVelocityTrapezoid: Trapezoid2f
    private var distanceVelocity: Vec2f
    private val distanceDistanceTrapezoid: Trapezoid2f
    private val sensitivity: Float

    // Gravity Rotation is disabled by default because it currently does not work as expected
    private val enableGravityRotation: Boolean

    init {

        // Create and configure components
        rotationDeltaTrapezoid = Trapezoid3f()
        activeGravityAverage = WindowAverage(parameters.lengthWindowGravity)
        activeNoiseAverage = WindowAverage(parameters.lengthWindowNoise)
        activeThreshold = OrThreshold(
            parameters.lengthThreshold,
            parameters.thresholdAcceleration,
            parameters.thresholdRotation
        )
        gravityInactiveAverage = WindowAverage3f(parameters.lengthGravity)
        gravityCurrent = Vec3f()
        distanceVelocityTrapezoid = Trapezoid2f()
        distanceDistanceTrapezoid = Trapezoid2f()
        distanceVelocity = Vec2f()
        sensitivity = parameters.sensitivity
        enableGravityRotation = parameters.enableGravityRotation
    }

    fun next(time: Float, delta: Float, acceleration: Vec3f, angularVelocity: Vec3f): Vec2f? {
        // Stage debug values
        debug!!.stageFloat(time)
        debug.stageVec3f(acceleration)
        debug.stageVec3f(angularVelocity)

        // Integrate rotation to distance, since that is used more often
        val rotationDelta = rotationDeltaTrapezoid.trapezoid(delta, angularVelocity)
        val active = active(acceleration, angularVelocity)
        debug.stageBoolean(active)
        val linearAcceleration = gravity(active, acceleration, rotationDelta)
        debug.stageVec2f(linearAcceleration)
        val distance = distance(delta, active, linearAcceleration, rotationDelta)
        debug.stageVec2f(distanceVelocity) // Do this here because it did not fit into the method
        debug.stageVec2f(distance)

        // Handle active changes "globally" for optimization
        lastActive = active
        debug.commit()
        return distance
    }

    fun active(acceleration: Vec3f, angularVelocity: Vec3f): Boolean {

        // Calculate the acceleration activation
        var acc = acceleration.xy().abs()
        debug!!.stageFloat(acc)

        // Remove gravity or rather lower frequencies
        val gravity = activeGravityAverage.avg(acc)
        debug.stageFloat(gravity)
        acc -= gravity
        acc = Math.abs(acc)

        // Remove noise
        acc = activeNoiseAverage.avg(acc)
        debug.stageFloat(acc)

        // Calculate the rotation activation
        val rot = Math.abs(angularVelocity.z)
        debug.stageFloat(rot)

        // Do the threshold
        return activeThreshold.active(acc, rot)
    }

    fun gravity(active: Boolean, acceleration: Vec3f, rotationDelta: Vec3f?): Vec2f? {

        // Differentiate between the user being active or not
        if (active) {

            // Reset average for next phase
            if (!lastActive) {
                gravityInactiveAverage.reset()
            }

            // Rotate current gravity
            if (enableGravityRotation) gravityCurrent!!.rotate(rotationDelta!!.copy().negative())
        } else {
            // Just calculate the average of the samples
            gravityCurrent = gravityInactiveAverage.avg(acceleration)
        }
        debug!!.stageVec3f(gravityCurrent)

        // Subtract the gravity
        return acceleration.xy().subtract(gravityCurrent!!.xy())
    }

    fun distance(
        delta: Float,
        active: Boolean,
        linearAcceleration: Vec2f?,
        rotationDelta: Vec3f?
    ): Vec2f? {

        // Only calculate if it is active for optimization
        return if (active) {

            // Counter-rotate the velocity
            distanceVelocity.rotate(-rotationDelta!!.z)

            // Integrate to distance
            distanceVelocity.add(distanceVelocityTrapezoid.trapezoid(delta, linearAcceleration))
            distanceDistanceTrapezoid.trapezoid(delta, distanceVelocity)!!.multiply(sensitivity)
        } else {

            // Reset stuff
            if (lastActive) {
                distanceVelocity = Vec2f()

                // Clean the trapezoids because they contain a last value
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