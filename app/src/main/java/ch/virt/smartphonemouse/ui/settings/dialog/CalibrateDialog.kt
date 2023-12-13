package ch.virt.smartphonemouse.ui.settings.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R

/**
 * This dialog is used in the settings menu when the user requests to calibrate the sampling rate.
 */
class CalibrateDialog : DialogFragment() {
    var dialog: AlertDialog? = null
    var positiveButton: Button? = null
    var calibrating = false
    private var finishedListener: DialogInterface.OnDismissListener? = null

    /**
     * Sets the listener that should be used to update the setting once the dialog is done.
     *
     * @param finishedListener dialog dismiss listener that is executed
     */
    fun setFinishedListener(finishedListener: DialogInterface.OnDismissListener?) {
        this.finishedListener = finishedListener
    }

    /**
     * Is called when the dialog is created.
     */
    private fun created() {
        dialog!!.setTitle(R.string.dialog_calibrate_title)
        dialog!!.setCanceledOnTouchOutside(false)
        setFragment(CalibrateBeginSubdialog())
    }

    private fun calibrate() {
        calibrating = true
        positiveButton!!.isEnabled = false
        positiveButton!!.setText(R.string.dialog_calibrate_done)
        setFragment(CalibrationHappeningSubdialog(object : CalibrationHappeningSubdialog.DoneListener {
            override fun done() {
                positiveButton!!.post { finished() }
            }
        }))
    }

    /**
     * Is called when the calibration process has finished.
     */
    private fun finished() {
        dialog!!.setTitle(R.string.dialog_calibrate_finished_title)
        dialog!!.setCanceledOnTouchOutside(true)
        positiveButton!!.isEnabled = true
        setFragment(CalibrateFinishedSubdialog())
    }

    /**
     * Sets the fragment of the dialog.
     *
     * @param fragment fragment to set
     */
    private fun setFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.calibrate_container, fragment).commit()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_calibrate, null))
            .setPositiveButton(R.string.dialog_calibrate_next, null)
        dialog = builder.create()
        dialog?.setTitle("-") // Add default title so it is shown
        dialog?.setOnShowListener(OnShowListener { dialogInterface: DialogInterface? ->
            positiveButton = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener(View.OnClickListener { b: View? -> if (!calibrating) calibrate() else dismiss() })
            created()
        })
        return dialog as Dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (finishedListener != null) finishedListener!!.onDismiss(dialog)
    }
}