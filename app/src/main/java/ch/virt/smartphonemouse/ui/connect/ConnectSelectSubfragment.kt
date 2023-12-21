package ch.virt.smartphonemouse.ui.connect

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
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.transmission.HostDevice
import ch.virt.smartphonemouse.ui.connect.ConnectSelectSubfragment.ListAdapter.ListResultListener
import ch.virt.smartphonemouse.ui.connect.dialog.AddDialog
import ch.virt.smartphonemouse.ui.connect.dialog.InfoDialog
import java.text.SimpleDateFormat
import java.util.Date

/**
 * This class is a sub fragment for the connect page.
 * On this fragment, the user can select or add a device, which he then can connect to.
 */
class ConnectSelectSubfragment
/**
 * Creates the sub fragment.
 * @param bluetooth bluetooth handler to handle bluetooth
 */(private val bluetooth: BluetoothHandler?) : Fragment(R.layout.subfragment_connect_select) {
    private var adapter: ListAdapter? = null
    private var list: RecyclerView? = null
    private var add: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.connect_select_items)
        add = view.findViewById(R.id.connect_select_add)
        add?.setOnClickListener(View.OnClickListener { v: View? -> add() })
        adapter = ListAdapter(bluetooth?.devices?.devices)
        adapter!!.setConnectListener(object : ListResultListener {
            override fun result(device: HostDevice?) {
                connect(device as HostDevice)
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

    /**
     * Opens the information dialog for the specified device.
     * @param device device
     */
    fun info(device: HostDevice) {
        val dialog = InfoDialog(bluetooth, device)
        dialog.setOnDismissListener { d: DialogInterface? -> adapter!!.notifyDataSetChanged() }
        dialog.show(this.parentFragmentManager, null)
    }

    /**
     * Connects to the known device specified
     * @param device device to connect to
     */
    fun connect(device: HostDevice) {
        bluetooth?.host?.connect(device)
    }

    /**
     * Starts the dialog to add a device.
     */
    private fun add() {
        val dialog = AddDialog(bluetooth)
        dialog.setOnDismissListener { d: DialogInterface? -> adapter!!.notifyDataSetChanged() }
        dialog.show(this.parentFragmentManager, null)
    }

    /**
     * This class is the list adapter for the recycler view which shows the known devices.
     */
    private class ListAdapter
    /**
     * Creates the list adapter.
     * @param devices devices to show
     */(private val devices: List<HostDevice?>?) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        private var connectListener: ListResultListener? = null
        private var infoListener: ListResultListener? = null

        /**
         * Sets the listener that gets called when the user clicks on an entry.
         * @param connectListener connect listener
         */
        fun setConnectListener(connectListener: ListResultListener?) {
            this.connectListener = connectListener
        }

        /**
         * Sets the listener that gets called when the user clicks on the info sign on an entry.
         * @param infoListener info listener
         */
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

        /**
         * This is the view holder for that recyclerview.
         */
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val name: TextView
            private val settings: ImageView
            private var device: HostDevice? = null

            /**
             * Creates the view holder.
             * @param view view to hold
             */
            init {
                name = view.findViewById(R.id.connect_select_list_item_name)
                settings = view.findViewById(R.id.connect_select_list_item_settings)
                view.setOnClickListener { v: View? -> connectListener!!.result(device) }
                settings.setOnClickListener { v: View? -> infoListener!!.result(device) }
            }

            /**
             * Sets the device the holder does represent.
             * @param device device
             */
            fun setDevice(device: HostDevice?) {
                this.device = device
                name.text = device?.name
            }
        }

        /**
         * This interface is a basic listener which is used for events regarding the recyclerview in this activity.
         */
        interface ListResultListener {
            /**
             * Gets called when an entry is clicked.
             * @param device device of that entry
             */
            fun result(device: HostDevice?)
        }
    }
}