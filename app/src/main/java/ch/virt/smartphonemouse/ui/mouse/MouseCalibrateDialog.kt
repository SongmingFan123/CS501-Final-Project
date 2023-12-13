package ch.virt.smartphonemouse.ui.mouse

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
 * This class is the dialog that is shown upon mouse calibration
 */
class MouseCalibrateDialog : DialogFragment() {
    private var dialog: AlertDialog? = null
    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var introduction = false

    /**
     * Is called when the dialog is created.
     */
    private fun created() {
        dialog!!.setTitle(R.string.dialog_mouse_calibrate_title_intro)
        introduction = true
        positiveButton!!.isEnabled = true
        negativeButton!!.visibility = View.VISIBLE
        setFragment(MouseMessageSubdialog(resources.getString(R.string.mouse_message_calibrate)))
    }

    /**
     * Is called when the user wants to go to the next page.
     */
    private operator fun next() {
        dialog!!.setTitle(R.string.dialog_mouse_calibrate_title_calibrate)
        if (introduction) {
            dialog!!.setCanceledOnTouchOutside(false)
            negativeButton!!.visibility = View.GONE
            positiveButton!!.isEnabled = false
            positiveButton!!.setText(R.string.dialog_mouse_calibrate_done)
            introduction = false

//            setFragment(new CalibrationHappeningSubdialog((r) -> positiveButton.post(this::finished)));
        } else dismiss()
    }

    /**
     * Is called when the calibration procedure is finished.
     */
    private fun finished() {
        dialog!!.setTitle(R.string.dialog_mouse_calibrate_title_finished)
        dialog!!.setCanceledOnTouchOutside(true)
        positiveButton!!.isEnabled = true
        setFragment(MouseMessageSubdialog(resources.getString(R.string.dialog_mouse_calibrate_finished)))
    }

    /**
     * Sets the current dialog fragment that should be displayed.
     *
     * @param fragment fragment to display
     */
    private fun setFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.mouse_container, fragment).commit()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_mouse, null))
            .setPositiveButton(R.string.dialog_mouse_next, null)
            .setNegativeButton(R.string.dialog_mouse_calibrate_cancel, null)
        dialog = builder.create()
        dialog?.setTitle("-") // Add default title so it is shown
        dialog?.setOnShowListener(OnShowListener { dialogInterface: DialogInterface? ->
            positiveButton = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener(View.OnClickListener { view: View? -> next() })
            negativeButton = dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
            created()
        })
        return dialog as Dialog
    }
}