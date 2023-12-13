package ch.virt.smartphonemouse.ui.settings.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import ch.virt.smartphonemouse.R

/**
 * This preference is a preference that opens a dialog and allows you to edit a float.
 */
class EditFloatPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextPreferenceStyle,
    defStyleRes: Int = 0
) : EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {
    private val showValueAsDescription: Boolean
    /**
     * Returns the unit that is displayed to the user.
     *
     * @return unit of the value
     */
    /**
     * Sets the unit that is displayed to the user.
     *
     * @param valueUnit unit of the entered value
     */
    var valueUnit: String?
    /**
     * Returns the minimum value that may be entered.
     *
     * @return minimum value
     */
    /**
     * Sets the minimum value that may be entered.
     *
     * @param minimumValue minimum value
     */
    var minimumValue = 0f
    /**
     * Returns the maximum value may be entered.
     *
     * @return maximum value
     */
    /**
     * Sets the maximum value that may be entered.
     *
     * @param maximumValue of the preference to be set
     */
    var maximumValue = 1000f
    private var value = 0f
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
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EditFloatPreference,
            defStyleAttr,
            defStyleRes
        )
        showValueAsDescription =
            a.getBoolean(R.styleable.EditFloatPreference_showValueAsSummary, false)
        val signed = a.getBoolean(R.styleable.EditFloatPreference_signed, false)
        valueUnit = a.getString(R.styleable.EditFloatPreference_valueUnit)
        a.recycle()
        if (valueUnit == null) valueUnit = ""
        if (signed) minimumValue = -maximumValue
        if (showValueAsDescription) summaryProvider =
            SummaryProvider { preference: Preference? -> this@EditFloatPreference.summary }
        setOnBindEditTextListener { editText: EditText ->
            editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or if (signed) InputType.TYPE_NUMBER_FLAG_SIGNED else 0x00
        }
    }

    /**
     * Returns the value of the preference.
     *
     * @return value
     */
    fun getValue(): Float {
        return value
    }

    /**
     * Sets the value of the preference.
     *
     * @param value value
     */
    fun setValue(value: Float) {
        val wasBlocking = shouldDisableDependents()
        this.value = Math.min(Math.max(value, minimumValue), maximumValue)
        persistFloat(value)
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking)
        }
        notifyChanged()
    }

    /**
     * Updates the contained value from the preference storage.
     */
    fun update() {
        setValue(getPersistedFloat(minimumValue))
    }

    override fun getSummary(): CharSequence {
        return if (showValueAsDescription) "$value $valueUnit" else super.getSummary()
    }

    override fun getText(): String {
        return java.lang.Float.toString(getValue())
    }

    override fun setText(text: String) {
        setValue(text.toFloat())
    }

    override fun shouldDisableDependents(): Boolean {
        return !this.isEnabled || value == 0f
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        var tmp: Float
        if (defaultValue == null) tmp = 0F
        else tmp = defaultValue as Float

        setValue(getPersistedFloat(tmp))
    }
}