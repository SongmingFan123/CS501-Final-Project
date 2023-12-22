package ch.virt.ezController.ui.connect

import android.content.DialogInterface
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
import ch.virt.ezController.transmission.BluetoothHandler
import ch.virt.ezController.transmission.HostDevice
import ch.virt.ezController.ui.connect.ConnectSelectSubfragment.ListAdapter.ListResultListener
import ch.virt.ezController.ui.connect.dialog.AddDialog
import ch.virt.ezController.ui.connect.dialog.InfoDialog

// This class is a sub fragment for the connect page.
class ConnectSelectSubfragment (private val bluetooth: BluetoothHandler?): Fragment(R.layout.subfragment_connect_select) {
    private var adapter: ListAdapter? = null
    private var list: RecyclerView? = null
    private var add: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.connect_select_items)
        add = view.findViewById(R.id.connect_select_add)
        add?.setOnClickListener({ v: View? -> add() })
        adapter = ListAdapter(bluetooth?.devices?.devices)
        adapter!!.setConnectListener(object : ListResultListener {
            override fun result(device: HostDevice?) {
                bluetooth?.host?.connect(device as HostDevice)
            }
        })
        adapter!!.setInfoListener(object : ListResultListener {
            override fun result(device: HostDevice?) {
                info(device as HostDevice)
            }
        })
        list?.setAdapter(adapter)
        list?.setLayoutManager(LinearLayoutManager(context))
    }

    // Opens the information dialog for the specified device.
    fun info(device: HostDevice) {
        val dialog = InfoDialog(bluetooth, device)
        dialog.setOnDismissListener { d: DialogInterface? -> adapter!!.notifyDataSetChanged() }
        dialog.show(this.parentFragmentManager, null)
    }

    // Starts the dialog to add a device.
    private fun add() {
        val dialog = AddDialog(bluetooth)
        dialog.setOnDismissListener { d: DialogInterface? -> adapter!!.notifyDataSetChanged() }
        dialog.show(this.parentFragmentManager, null)
    }

    // This class is the list adapter for the recycler view which shows the known devices.
    private class ListAdapter (private val devices: List<HostDevice?>?):
        RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        private var connectListener: ListResultListener? = null
        private var infoListener: ListResultListener? = null

        fun setConnectListener(connectListener: ListResultListener?) {
            this.connectListener = connectListener
        }

        fun setInfoListener(infoListener: ListResultListener?) {
            this.infoListener = infoListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_connect_select, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setDevice(devices!![position])
        }

        override fun getItemCount(): Int {
            return devices!!.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val name: TextView
            private val settings: ImageView
            private var device: HostDevice? = null

            init {
                name = view.findViewById(R.id.connect_select_list_item_name)
                settings = view.findViewById(R.id.connect_select_list_item_settings)
                view.setOnClickListener { v: View? -> connectListener!!.result(device) }
                settings.setOnClickListener { v: View? -> infoListener!!.result(device) }
            }

            fun setDevice(device: HostDevice?) {
                this.device = device
                name.text = device?.name
            }
        }

        // This interface is a basic listener which is used for events regarding the recyclerview in this activity.
        interface ListResultListener {
            fun result(device: HostDevice?)
        }
    }
}