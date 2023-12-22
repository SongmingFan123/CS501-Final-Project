package ch.virt.ezController.ui.connect.dialog

import android.bluetooth.BluetoothClass
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.virt.ezController.R
import ch.virt.ezController.transmission.BluetoothDiscoverer
import ch.virt.ezController.transmission.BluetoothDiscoverer.DiscoveredDevice
import ch.virt.ezController.transmission.BluetoothHandler
import ch.virt.ezController.ui.connect.dialog.AddSelectSubdialog.ListAdapter.SelectListener

// This class holds a sub page for the add dialog that is used to display discovered devices and to select them.
class AddSelectSubdialog (private val bluetooth: BluetoothHandler?) : Fragment(R.layout.subdialog_add_select) {
    private var scanning: Button? = null
    private var list: RecyclerView? = null
    private var adapter: ListAdapter? = null
    private var selectListener: SelectListener? = null

    // Starts the discovery.
    private fun startDiscovery() {
        bluetooth?.discoverer?.reset()
        bluetooth?.discoverer?.startDiscovery()
    }

    // This method updates the scan button.
    private fun discoveryUpdated(status: Boolean) {
        if (status) {
            scanning!!.isEnabled = false
            scanning!!.setText(R.string.dialog_add_select_scanning)
        } else {
            scanning!!.isEnabled = true
            scanning!!.setText(R.string.dialog_add_select_rescan)
        }
    }

    // Sets the listener for when a device has been selected.
    fun setSelectListener(selectListener: SelectListener?) {
        this.selectListener = selectListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.add_select_list)
        scanning = view.findViewById(R.id.add_select_scanning)
        adapter = ListAdapter(bluetooth?.discoverer?.devices)
        adapter!!.setSelectListener(selectListener)
        list?.setAdapter(adapter)
        list?.setLayoutManager(LinearLayoutManager(context))
        scanning?.setOnClickListener(View.OnClickListener { v: View? -> startDiscovery() })
        bluetooth?.discoverer
            ?.setUpdateListener(object : BluetoothDiscoverer.UpdateListener {
                override fun update(devices: List<DiscoveredDevice>?) {
                    adapter!!.notifyDataSetChanged()
                }
            })
        bluetooth?.discoverer?.setScanListener(object : BluetoothDiscoverer.ScanListener {
            override fun changed(scanning: Boolean) {
                discoveryUpdated(scanning)
            }
        })
        startDiscovery()
    }

    // This list adapter handles the recycler view that shows all the discovered devices.
    class ListAdapter(private val devices: List<DiscoveredDevice?>?) :
        RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        private var selectListener: SelectListener? = null

        // Sets the listener that is called when a device is selected.
        fun setSelectListener(selectListener: SelectListener?) {
            this.selectListener = selectListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_select, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.populate(devices!![position])
        }

        override fun getItemCount(): Int {
            return devices!!.size
        }

        // This class is the view holder for the recycler view that shows the discovered devices.
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val label: TextView
            private val icon: ImageView
            private var device: DiscoveredDevice? = null

            // Creates the view holder.
            init {
                view.setOnClickListener { v: View? -> selectListener!!.called(device) }
                label = view.findViewById(R.id.add_select_list_item_text)
                icon = view.findViewById(R.id.add_select_list_item_icon)
            }

            // Populates the view with the data about one device.
            fun populate(device: DiscoveredDevice?) {
                this.device = device
                label.text = device?.name

                // Only covers the three major types
                if (device?.majorClass == BluetoothClass.Device.Major.PHONE) icon.setImageResource(
                    R.drawable.device_add_smartphone
                ) else if (device?.majorClass == BluetoothClass.Device.Major.COMPUTER) icon.setImageResource(
                    R.drawable.device_add_computer
                ) else icon.setImageResource(R.drawable.device_add_other)
            }
        }

        // This interface is a basic listener for when a list entry is selected.
        interface SelectListener {
            fun called(device: DiscoveredDevice?)
        }
    }
}