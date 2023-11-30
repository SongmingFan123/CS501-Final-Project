package ch.virt.smartphonemouse.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R

/**
 * This sub fragment of the home page is shown when the smartphone does not support the bluetooth id profile.
 */
class HomeUnsupportedSubfragment
/**
 * Creates this sub fragment.
 */
    : Fragment(R.layout.subfragment_home_unsupported) {
    private var playstoreLink: Button? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playstoreLink = view.findViewById(R.id.home_unsupported_playstore)
        playstoreLink?.setOnClickListener(View.OnClickListener { v: View? ->
            Toast.makeText(
                view.context,
                "Currently not published",
                Toast.LENGTH_SHORT
            ).show()
        })
    }
}