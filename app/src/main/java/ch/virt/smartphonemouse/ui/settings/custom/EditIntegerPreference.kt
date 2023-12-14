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
 * This is a preference that opens a dialog for the user to set a integer value.
 */
class EditIntegerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextPreferenceStyle,
    defStyleRes: Int = 0
) : EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {
    /**
     * Whether the value should be shown as description.
     *
     * @return is shown
     */
    /**
     * Sets whether the value should be shown as the description of the preference.
     *
     * @param showValueAsDescription sets whether it should be shown as a description
     */
    var isShowValueAsDescription: Boolean
    /**
     * Returns the unit of the value of the preference.
     *
     * @return value unit
     */
    /**
     * Sets the unit of the value of the preference.
     *
     * @param valueUnit unit of the value
     */
    var valueUnit: String?
    /**
     * Returns the minimum value of the preference.
     *
     * @return minimum value
     */
    /**
     * Sets the minimum value that may be entered to the preference.
     *
     * @param minimumValue minimum value
     */
    var minimumValue = 0
    /**
     * Returns the maximum value of the preference.
     *
     * @return maximum value
     */
    /**
     * Sets the maximum value that may be entered to the preference.
     *
     * @param maximumValue maximum value
     */
    var maximumValue = 100000
    private var value = 0
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
            R.styleable.EditIntegerPreference,
            defStyleAttr,
            defStyleRes
        )
        isShowValueAsDescription =
            a.getBoolean(R.styleable.EditIntegerPreference_showValueAsSummary, false)
        val signed = a.getBoolean(R.styleable.EditFloatPreference_signed, false)
        valueUnit = a.getString(R.styleable.EditIntegerPreference_valueUnit)
        a.recycle()
        if (valueUnit == null) valueUnit = ""
        if (signed) minimumValue = -maximumValue
        if (isShowValueAsDescription) summaryProvider =
            SummaryProvider { preference: Preference? -> this@EditIntegerPreference.summary }
        setOnBindEditTextListener { editText: EditText ->
            editText.inputType =
                InputType.TYPE_CLASS_NUMBER or if (signed) InputType.TYPE_NUMBER_FLAG_SIGNED else 0x00
        }
    }

    /**
     * Returns the value of the preference.
     *
     * @return value of the preference
     */
    fun getValue(): Int {
        return value
    }

    /**
     * Sets the value of the preference.
     *
     * @param value value of the preference
     */
    fun setValue(value: Int) {
        val wasBlocking = shouldDisableDependents()
        this.value = Math.min(Math.max(value, minimumValue), maximumValue)
        persistInt(value)
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking)
        }
        notifyChanged()
    }

    /**
     * Updates the preference to the current stored value.
     */
    fun update() {
        setValue(getPersistedInt(minimumValue))
    }

    override fun getSummary(): CharSequence {
        return if (isShowValueAsDescription) "$value $valueUnit" else super.getSummary()!!
    }

    override fun getText(): String {
        return Integer.toString(getValue())
    }

    override fun setText(text: String?) {
        setValue(text!!.toInt())
    }

    override fun shouldDisableDependents(): Boolean {
        return !this.isEnabled || value == 0
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setValue(getPersistedInt((if (defaultValue == null) 0 else defaultValue as Int?)!!))
    }
}