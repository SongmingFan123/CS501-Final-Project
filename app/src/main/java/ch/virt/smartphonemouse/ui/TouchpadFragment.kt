package ch.virt.smartphonemouse.ui
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R
class TouchpadFragment:Fragment(R.layout.fragment_touchpad) {

    private var root: RelativeLayout? = null
    private var visuals = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Set system view visibility
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!visuals) requireActivity().window.insetsController!!.hide(WindowInsets.Type.statusBars())
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.mandatorySystemGestures())
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.systemGestures())
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.navigationBars())
        } else {
            if (!visuals) requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {

        // Unset system view visibility
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view.findViewById(R.id.touchpad_root)
    }
}