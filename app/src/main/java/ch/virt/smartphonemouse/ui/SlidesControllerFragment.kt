package ch.virt.smartphonemouse.ui

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import ch.virt.smartphonemouse.R
import ch.virt.smartphonemouse.mouse.MouseInputs

class SlidesControllerFragment (private val mouse : MouseInputs?): Fragment(R.layout.fragment_slides_controller) {
    private var root: RelativeLayout? = null
    private var theme = false
    private var visuals = false
    private var vibrations = false
    private var buttonIntensity = 0
    private var buttonLength = 0
    private var vibrator: Vibrator? = null
    private var scrollDistance = 3
    private var previousButton : ImageButton? = null
    private var nextButton : ImageButton? = null
    //create slide controller view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_slides_controller, container, false)
        root = rootView.findViewById(R.id.slides_controller_root)
        previousButton = rootView.findViewById(R.id.previous_button)
        nextButton = rootView.findViewById(R.id.next_button)
        previousButton?.setOnClickListener {
            vibrate(buttonLength, buttonIntensity)
            mouse!!.changeWheelPosition(scrollDistance)
        }
        nextButton?.setOnClickListener {
            vibrate(buttonLength, buttonIntensity)
            mouse!!.changeWheelPosition(-scrollDistance)
        }
        return rootView
    }
    //destroy slides controller view
    override fun onDestroyView() {
        requireActivity().window.statusBarColor =
            resources.getColor(R.color.design_default_color_primary_dark)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!visuals) requireActivity().window.insetsController!!.show(WindowInsets.Type.statusBars())
            requireActivity().window.insetsController!!.show(WindowInsets.Type.mandatorySystemGestures())
            requireActivity().window.insetsController!!.show(WindowInsets.Type.systemGestures())
            requireActivity().window.insetsController!!.show(WindowInsets.Type.navigationBars())
        } else {
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        super.onDestroyView()
    }
    // create view and initialization of slides controller
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view.findViewById(R.id.slides_controller_root)
        root?.setBackgroundResource(if (theme) R.color.mouse_background_dark else R.color.mouse_background_light)
    }

    //If the vibrations of the device are enabled, the device vibrates

    private fun vibrate(length: Int, intensity: Int) {
        if (vibrations) vibrator!!.vibrate(
            VibrationEffect.createOneShot(
                length.toLong(),
                intensity
            )
        )
    }
}