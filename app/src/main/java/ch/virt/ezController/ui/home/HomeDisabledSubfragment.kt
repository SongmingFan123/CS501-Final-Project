package ch.virt.ezController.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import ch.virt.ezController.R
import ch.virt.ezController.transmission.BluetoothHandler


//Sub fragment that gets the home screen displayed while bluetooth is disabled.
class HomeDisabledSubfragment
/**
 * Creates the sub fragment.
 *
 * @param handler bluetooth handler to use to refresh
 */(private val handler: BluetoothHandler?) : Fragment(R.layout.subfragment_home_disabled) {
    private var refresh: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh = view.findViewById(R.id.home_disabled_recheck)
        refresh?.setOnClickListener(View.OnClickListener { v: View? -> handler!!.reInit() })
    }
}