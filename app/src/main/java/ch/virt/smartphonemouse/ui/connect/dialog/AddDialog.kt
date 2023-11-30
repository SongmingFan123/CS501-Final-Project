package ch.virt.smartphonemouse.ui.connect.dialog

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothDiscoverer.DiscoveredDevice
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.transmission.HostDevice

/**
 * This dialog is shown when the user wants to add a device.
 */
class AddDialog
/**
 * Creates an add dialog.
 * @param bluetoothHandler bluetooth handler to use
 */(private val bluetoothHandler: BluetoothHandler?) : DialogFragment() {
    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var neutralButton: Button? = null
    private var dismissListener: DialogInterface.OnDismissListener? = null
    private var dialog: AlertDialog? = null
    private var state = 0
    private var currentFragment: Fragment? = null
    private var target: DiscoveredDevice? = null
    private var requestLocation: ActivityResultLauncher<String>? = null
    private var enableLocation: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Location requests
        requestLocation = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            checkPermission(isGranted)
        }
        enableLocation =
            registerForActivityResult(StartActivityForResult()) { result: ActivityResult? -> positiveButton!!.post { checkSetting() } }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_add, null))
            .setPositiveButton(R.string.dialog_add_next, null)
            .setNegativeButton(R.string.dialog_add_cancel, null)
            .setNeutralButton("-", null)
        dialog = builder.create()
        dialog?.setTitle("-") // Add default title so it is shown
        dialog?.setOnShowListener(OnShowListener { dialogInterface: DialogInterface? ->
            positiveButton = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            negativeButton = dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
            neutralButton = dialog?.getButton(AlertDialog.BUTTON_NEUTRAL)
            positiveButton?.setOnClickListener(View.OnClickListener { v: View? -> onNext() })
            neutralButton?.setOnClickListener(View.OnClickListener { v: View? -> onNeutral() })
            created()
        })
        return dialog as Dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (bluetoothHandler?.discoverer?.isScanning == true) bluetoothHandler.discoverer!!
            .stopDiscovery()
        super.onDismiss(dialog)
        dismissListener!!.onDismiss(dialog)
    }

    /**
     * This method gets called when the dialog is shown.
     */
    private fun created() {
        neutralButton!!.visibility = View.GONE
        dialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM) // Enable keyboard to be working
        showRequestPermission()
    }

    /**
     * This method sets a sub fragment.
     * @param fragment fragment to be set
     */
    private fun setFragment(fragment: Fragment) {
        currentFragment = fragment
        childFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.add_container, currentFragment!!).commit()
    }

    /**
     * This method shows the fragment that requests the permission to discover devices.
     */
    fun showRequestPermission() {
        state = REQUEST_PERMISSION_STATE
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showRequestSetting() // Get on if already granted
            return
        }
        setFragment(AddRequestPermissionSubdialog())
        dialog!!.setTitle(R.string.dialog_add_request_permission_title)
        positiveButton!!.visibility = View.VISIBLE
        neutralButton!!.visibility = View.VISIBLE
        neutralButton!!.setText(R.string.dialog_add_select_manual)
    }

    /**
     * This method shows the fragment that requests the user to turn on location in order to discover devices.
     */
    fun showRequestSetting() {
        state = REQUEST_SETTING_STATE
        if (LocationManagerCompat.isLocationEnabled((requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager))) {
            showSelect() // Get on if already enabled
            return
        }
        setFragment(AddRequestSettingSubdialog())
        dialog!!.setTitle(R.string.dialog_add_request_setting_title)
        positiveButton!!.visibility = View.VISIBLE
        neutralButton!!.visibility = View.VISIBLE
        neutralButton!!.setText(R.string.dialog_add_select_manual)
    }

    /**
     * This method shows the fragment where the user can select form nearby devices.
     */
    fun showSelect() {
        state = SELECT_STATE
        val fragment = AddSelectSubdialog(bluetoothHandler)
        fragment.setSelectListener(object : AddSelectSubdialog.ListAdapter.SelectListener {
            override fun called(device: DiscoveredDevice?) {
                selected(device)
            }
        })
        setFragment(fragment)
        dialog!!.setTitle(R.string.dialog_add_select_title)
        positiveButton!!.visibility = View.GONE
        neutralButton!!.visibility = View.VISIBLE
        neutralButton!!.setText(R.string.dialog_add_select_manual)
    }

    /**
     * This method shows the fragment where the user can enter their device details manually.
     */
    fun showManual() {
        state = MANUAL_STATE
        setFragment(AddManualSubdialog())
        dialog!!.setTitle(getString(R.string.dialog_add_manual_title))
        positiveButton!!.visibility = View.VISIBLE
        neutralButton!!.visibility = View.GONE
    }

    /**
     * This method shows the fragment which tells the user that they have to remove the target device from their bonded devices.
     */
    fun showBonded() {
        state = BONDED_STATE
        setFragment(AddBondedSubdialog(bluetoothHandler, target))
        dialog!!.setTitle(getString(R.string.dialog_add_bonded_title))
        positiveButton!!.visibility = View.VISIBLE
        neutralButton!!.visibility = View.GONE
    }

    /**
     * Adds the target device to the device storage and displays the success message.
     */
    fun finished() {
        bluetoothHandler?.devices?.addDevice(HostDevice(target?.address, target?.name))
        Toast.makeText(context, "Add successfully", Toast.LENGTH_SHORT).show()
    }

    /**
     * Proceeds after a device has been selected.
     * @param device selected device.
     */
    fun selected(device: DiscoveredDevice?) {
        target = device
        if (device != null) {
            if (bluetoothHandler!!.isBonded(device.address)) showBonded() else finished()
        }
    }

    /**
     * This method is called when the positive button is clicked.
     */
    fun onNext() {
        when (state) {
            REQUEST_PERMISSION_STATE -> requestPermission()
            REQUEST_SETTING_STATE -> requestSetting()
            MANUAL_STATE -> if ((currentFragment as AddManualSubdialog?)!!.check()) selected(
                (currentFragment as AddManualSubdialog?)!!.createDevice()
            )

            BONDED_STATE -> finished()
            SUCCESS_STATE, ALREADY_STATE -> {
                dismiss()
                onDismiss(getDialog()!!)
            }
        }
    }

    /**
     * This method is called when the neutral button is clicked.
     */
    fun onNeutral() {
        if (state == SELECT_STATE || state == REQUEST_PERMISSION_STATE || state == REQUEST_SETTING_STATE) showManual()
    }

    /**
     * Launches the request permission intent to request location permissions.
     */
    fun requestPermission() {
        requestLocation!!.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Proceeds after the permission may have been granted.
     * @param isGranted whether it was granted
     */
    fun checkPermission(isGranted: Boolean) {
        if (isGranted) {
            showRequestSetting()
        } else (currentFragment as AddRequestPermissionSubdialog?)!!.showError()
    }

    /**
     * Launches the request intent to request to turn on location.
     */
    fun requestSetting() {
        if (LocationManagerCompat.isLocationEnabled((requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager))) {
            showSelect()
        } else {
            enableLocation!!.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    /**
     * Checks whether the location setting was turned on.
     */
    fun checkSetting() {
        if (LocationManagerCompat.isLocationEnabled((requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager))) {
            showSelect()
        } else {
            (currentFragment as AddRequestSettingSubdialog?)!!.showError()
        }
    }

    /**
     * Sets the dismiss listener.
     * The listener should be set before the dialog is shown.
     *
     * @param dismissListener dismiss listener that will be passed to the dialog
     * @see Dialog.setOnDismissListener
     */
    fun setOnDismissListener(dismissListener: DialogInterface.OnDismissListener?) {
        this.dismissListener = dismissListener
    }

    companion object {
        private const val SELECT_STATE = 0
        private const val MANUAL_STATE = 1
        private const val BONDED_STATE = 2
        private const val SUCCESS_STATE = 3
        private const val ALREADY_STATE = 4
        private const val REQUEST_PERMISSION_STATE = 5
        private const val REQUEST_SETTING_STATE = 6
    }
}