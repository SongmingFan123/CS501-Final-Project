package ch.virt.smartphonemouse.ui.settings.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.customization.CalibrationHandler
import ch.virt.smartphonemouse.mouse.Calibration

/**
 * This fragment for a dialog is used to handle the calibration of the sampling rate.
 */
class CalibrationHappeningSubdialog
/**
 * Creates the sub dialog fragment.
 *
 * @param doneListener listener that gets called when the calibration process is finished.
 */(private val doneListener: DoneListener) : Fragment(R.layout.subdialog_calibrate_happening) {
    private var time: TextView? = null
    private var calibrator: CalibrationHandler? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calibrator = CalibrationHandler(context)
        time = view.findViewById(R.id.calibrate_samplingrate_time)
        time?.setText(resources.getString(R.string.dialog_calibrate_happening_init))
        calibrator!!.calibrate(object : Calibration.StateListener {
            override fun update(state: Int) {
                time?.post(Runnable {
                    if (state == Calibration.Companion.STATE_SAMPLING) time?.setText(
                        resources.getString(R.string.dialog_calibrate_happening_sampling)
                    ) else if (state == Calibration.Companion.STATE_NOISE) time?.setText(
                        resources.getString(R.string.dialog_calibrate_happening_noise)
                    ) else if (state == Calibration.Companion.STATE_END) doneListener.done()
                })
            }
        })
    }

    interface DoneListener {
        fun done()
    }
}