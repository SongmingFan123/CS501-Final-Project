package ch.virt.ezController.mouse.math

// This class represents a vec3 average which operates in a window.
class WindowAverage3f(length: Int) {
    private var index = 0
    private var elements: Array<Vec3f?>

    init {
        elements = arrayOfNulls(length)
    }
    //window average in vec3f
    fun avg(next: Vec3f?): Vec3f? {
        elements[index % elements.size] = next
        index++
        val total = Vec3f()
        val amount = Math.min(elements.size, index)
        for (i in 0 until amount) {
            total.add(elements[i])
        }
        return total.divide(amount.toFloat())
    }

    fun reset() {
        elements = arrayOfNulls(elements.size)
        index = 0
    }
}