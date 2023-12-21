package ch.virt.smartphonemouse.mouse

import android.util.Log
import java.lang.Math.abs
import java.lang.Math.exp

private val TAG = "KalmanFilter"
class KalmanFilter() {
    val v = 20
    val Thresh = 0.3
    val dampRate = 0.8
    var preData = 0.0
    var variance = 0.0
    val Q = 0.3
    val R = 0.03

    var xPreData = 0.0
    var yPreData = 0.0
    var xVar = 0.1
    var yVar = 0.1
    var vx = 0.0
    var vy = 0.0

    //Calibration
    var numCali = 0
    val caliAcc = mutableListOf<List<Double>>()
    var axave = 0.0
    var ayave = 0.0

    fun calibration(ax: Float, ay: Float): Boolean {
//        Log.d(TAG, "Begin to calibrate, please put the mobile phone on the surface and do not move")
        caliAcc.add(listOf(ax.toDouble(), ay.toDouble()))
        numCali++

        Log.d(TAG, "$numCali: [$ax, $ay]")

        if(numCali != 1000) return false
        val arrayCaliAcc = caliAcc.toTypedArray()
        axave = arrayCaliAcc.map { it[0] }.average()
        ayave = arrayCaliAcc.map { it[1] }.average()

        Log.d(TAG, "Calibration result: $axave, $ayave")
        Log.d(TAG, "It is now available to use your mobile phone to control the cursor")
        return true
    }

    fun getDis(ax: Float, ay: Float): Pair<Float, Float> {
        // Calibration steps
        val axCalibrated = ax.toDouble() - axave
        val ayCalibrated = ay.toDouble() - ayave
//        val axCalibrated = ax.toDouble() - 0.0
//        val ayCalibrated = ay.toDouble() - 0.0
//        if(abs(axCalibrated) > 0.1 && abs(ayCalibrated) > 0.1)
//            Log.d(TAG, "Calibrated Acc: " + axCalibrated + " " + ayCalibrated)

        // Apply Kalman filter for both axes
        val (filteredAx, updatedXVar) = kalmanFilter(axCalibrated, xPreData, xVar, Q, R)
        val (filteredAy, updatedYVar) = kalmanFilter(ayCalibrated, yPreData, yVar, Q, R)

        // Update variables for next iteration
        xPreData = filteredAx
        xVar = updatedXVar
        yPreData = filteredAy
        yVar = updatedYVar

        // Dampening the speed
        vx *= dampRate
        vy *= dampRate
        Log.d(TAG, "Speed: " + vx + " " + vy)

        // Adjust speed if acceleration exceeds the threshold
        if (abs(filteredAx) > Thresh) {
//            val dvx = exp(-abs(vx)) * filteredAx * v
            val dvx = exp(-abs(vx)) * filteredAx * v
            vx += dvx.toFloat()
        }
        if (abs(filteredAy) > Thresh) {
            val dvy = exp(-abs(vy)) * filteredAy * v
            vy += dvy.toFloat()
        }

        // Update cursor position (assuming 'win32api' is used to control cursor)
//        val resx: Float = if(vx > 0.001) vx.toFloat() else 0.0f
//        val resy: Float = if(vy > 0.001) vy.toFloat() else 0.0f
//        return Pair(resx, resy)
        return Pair(vx.toFloat(), vy.toFloat())
    }


    fun kalmanFilter(data: Double, preData: Double, varValue: Double, Q: Double, R: Double): Pair<Double, Double> {
        var preVar = varValue + Q
        val K = preVar / (preVar + R)
        val outputData = preData + K * (data - preData)
        var varVar = (1 - K) * preVar
//        preData = outputData
        return Pair(outputData, varVar)
    }
}