package ch.virt.smartphonemouse.ui.settings.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SeekBarPreference
import ch.virt.smartphonemouse.R

/**
 * This preference is a seek bar preference that does seek for integers but is
 */
class SeekIntegerPreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarPreferenceStyle,
    defStyleRes: Int = 0
) : SeekBarPreference(context, attrs, defStyleAttr, defStyleRes) {
    /**
     * Returns the maximum value of the preference.
     *
     * @return maximum value
     */
    /**
     * Sets the maximum value of the preference.
     *
     * @param maximum maximum value.
     */
    var maximum = 0
    /**
     * Returns the minimum value of the preference.
     *
     * @return minimum value
     */
    /**
     * Sets the minimum value of the reference
     *
     * @param minimum minimum value
     */
    var minimum = 0
    private var steps = 0
    /**
     * Creates a preference.
     *
     * @param context      context for the preference to be in
     * @param attrs        attributes
     * @param defStyleAttr style attributes
     * @param defStyleRes  style resources
     */
    /**
     * Creates a preference.
     *
     * @param context      context for the preference to be in
     * @param attrs        attributes
     * @param defStyleAttr style attributes
     */
    /**
     * Creates a preference.
     *
     * @param context context for the preference to be in
     * @param attrs   attributes
     */
    /**
     * Creates a preference.
     *
     * @param context context for the preference to be in
     */
    init {
        setSteps(20000)
        maximum = 100
        minimum = -100
    }

    /**
     * Returns the steps the seek bar can be in.
     *
     * @return amount of steps
     */
    fun getSteps(): Int {
        return steps
    }

    /**
     * Sets the steps the seek bar can be positioned in.
     *
     * @param steps amount of steps
     */
    fun setSteps(steps: Int) {
        this.steps = steps
        min = 0
        max = steps
    }

    val realValue: Int
        /**
         * Returns the real value of the preference. Use this instead of realValue.
         *
         * @return value of the preference
         */
        get() = (value / steps.toFloat() * (maximum - minimum) + minimum).toInt()

    /**
     * Updates the preference to the stored value.
     */
    fun update() {
        onSetInitialValue(null)
    }

    override fun persistInt(value: Int): Boolean {
        // Have to use persistence methods, because otherwise, the variables are not accessible enough
        return super.persistInt((value / steps.toFloat() * (maximum - minimum) + minimum).toInt())
    }

    override fun getPersistedInt(defaultReturnValue: Int): Int {
        // Have to use persistence methods, because otherwise, the variables are not accessible enough
        return ((super.getPersistedInt(minimum) - minimum) / (maximum - minimum).toFloat() * steps).toInt()
    }
}