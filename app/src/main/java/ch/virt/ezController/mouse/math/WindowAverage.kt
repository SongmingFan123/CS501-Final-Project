package ch.virt.ezController.mouse.math

// This class represents a vec2 average which operates in a window.
class WindowAverage(length: Int) {
    private var index = 0
    private var elements: FloatArray

    init {
        elements = FloatArray(length)
    }
    //window average
    fun avg(next: Float): Float {
        elements[index % elements.size] = next
        index++
        var total = 0f
        val amount = Math.min(elements.size, index)
        for (i in 0 until amount) {
            total += elements[i]
        }
        return total / amount
    }
}