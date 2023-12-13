package ch.virt.smartphonemouse.ui.settings.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SeekBarPreference
import ch.virt.smartphonemouse.R

/**
 * This is a Seek Bar preference that stores a float.
 */
class SeekFloatPreference @JvmOverloads constructor(
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
     * @param maximum maximum value
     */
    var maximum = 0f
    /**
     * Returns the minimum value of the preference.
     *
     * @return minimum value
     */
    /**
     * Sets the minimum value of the preference.
     *
     * @param minimum minimum value
     */
    var minimum = 0f
    var steps = 0
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

        // Set a ludicrous min, max and amount of steps, because all data will be fitted to it for some reason
        // FIXME: Investigate this and fix it
        setStepsAndRelated(10000000)
        maximum = 50000f
        minimum = -50000f
    }

    /**
     * Returns how many steps are present on the progress bar.
     *
     * @return steps on the bar
     */

    /**
     * Sets the steps present on the progress bar.
     *
     * @param steps steps on the bar
     */
    fun setStepsAndRelated(steps: Int) {
        this.steps = steps
        min = 0
        max = steps
    }

    val realValue: Float
        /**
         * Returns the real value that the preference holds. Use this instead of getValue.
         *
         * @return value
         */
        get() = value / steps.toFloat() * (maximum - minimum) + minimum

    /**
     * Updates the preference to the values stored in the storage.
     */
    fun update() {
        onSetInitialValue(null)
    }

    override fun persistInt(value: Int): Boolean {
        // Have to use persistence methods, because otherwise, the variables are not accessible enough
        return super.persistFloat(value / steps.toFloat() * (maximum - minimum) + minimum)
    }

    override fun getPersistedInt(defaultReturnValue: Int): Int {
        // Have to use persistence methods, because otherwise, the variables are not accessible enough
        return ((super.getPersistedFloat(minimum) - minimum) / (maximum - minimum) * steps).toInt()
    }
}