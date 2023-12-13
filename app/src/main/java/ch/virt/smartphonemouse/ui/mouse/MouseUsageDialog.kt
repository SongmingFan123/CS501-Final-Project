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
 * This dialog is shown when the user opens the mouse fragment. It tells the user how to use the mouse.
 */
class MouseUsageDialog(private val finishedListener: UsageFinishedListener) : DialogFragment() {
    private var dialog: AlertDialog? = null
    private var positiveButton: Button? = null
    private var index = 0

    /**
     * Returns a message to show the user at the given index.
     *
     * @param index index of the message
     * @return resource string id of the message
     */
    private fun getCurrentMessage(index: Int): Int {
        return when (index) {
            0 -> R.string.dialog_mouse_usage_even
            1 -> R.string.dialog_mouse_usage_move
            2 -> R.string.dialog_mouse_usage_buttons
            3 -> R.string.dialog_mouse_usage_return
            else -> 0
        }
    }

    private val messageAmount: Int
        /**
         * Returns how many messages can be displayed.
         *
         * @return amount of messages
         */
        private get() = 4

    /**
     * Is called when the dialog is created.
     */
    private fun create() {
        showFragment()
        dialog!!.setCanceledOnTouchOutside(false)
    }

    /**
     * Is called when the user skips to the next page.
     */
    private operator fun next() {
        if (index == messageAmount) {
            dismiss()
            return
        }
        index++
        if (index == messageAmount) {
            dialog!!.setCanceledOnTouchOutside(true)
            setFragment(MouseUsageFinishedSubdialog())
        } else showFragment()
    }

    /**
     * Shows a message fragment with the current index.
     */
    private fun showFragment() {
        setFragment(MouseMessageSubdialog(resources.getString(getCurrentMessage(index))))
    }

    /**
     * Displays a fragment in the respective fragment container.
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
            .setPositiveButton(R.string.dialog_mouse_usage_next, null)
        dialog = builder.create()
        dialog?.setTitle(R.string.dialog_mouse_usage_title)
        dialog?.setOnShowListener(OnShowListener { dialogInterface: DialogInterface? ->
            positiveButton = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener(View.OnClickListener { view: View? -> next() })
            create()
        })
        return (dialog as AlertDialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        finishedListener.finished()
        super.onDismiss(dialog)
    }

    /**
     * This interface is a basic listener for when the user has finished reading the instructions.
     */
    interface UsageFinishedListener {
        /**
         * Called when the user is finished.
         */
        fun finished()
    }
}