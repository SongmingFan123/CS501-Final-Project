package ch.virt.smartphonemouse.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.R

class SlidesControllerFragment: Fragment(R.layout.fragment_slides_controller) {
    private var root: RelativeLayout? = null
    private var width = 0
    private var height = 0
    private var theme = false // false = light, true = dark

    // Feedback
    private var visuals = false
    private var buttonsStrokeWeight = 0
    private var viewIntensity = 0f
    private var vibrations = false
    private var buttonIntensity = 0
    private var buttonLength = 0
    private var scrollIntensity = 0
    private var scrollLength = 0
    private var specialIntensity = 0
    private var specialLength = 0
    private var vibrator: Vibrator? = null

    // Buttons
    private var buttonsHeight = 0f
    private var buttonsMiddleWidth = 0f
    private var leftX = 0
    private var leftY = 0
    private var leftWidth = 0
    private var leftHeight = 0
    private var rightX = 0
    private var rightY = 0
    private var rightWidth = 0
    private var rightHeight = 0
    private var middleX = 0
    private var middleY = 0
    private var middleWidth = 0
    private var middleHeight = 0


    // Middle Specific
    private var middleClickWait = 0
    private var scrollThreshold = 0


    /**
     * Reads the settings for the fragment from the preferences.
     */
    private fun readSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        theme = prefs.getString("interfaceTheme", "dark") == "dark"
        scrollThreshold = prefs.getInt("interfaceBehaviourScrollStep", 50)
        middleClickWait = prefs.getInt("interfaceBehaviourSpecialWait", 300)
        visuals = prefs.getBoolean("interfaceVisualsEnable", true)
        buttonsStrokeWeight = prefs.getInt("interfaceVisualsStrokeWeight", 4)
        viewIntensity = prefs.getFloat("interfaceVisualsIntensity", 0.5f)
        vibrations = prefs.getBoolean("interfaceVibrationsEnable", true)
        buttonIntensity = prefs.getInt("interfaceVibrationsButtonIntensity", 100)
        buttonLength = prefs.getInt("interfaceVibrationsButtonLength", 30)
        scrollIntensity = prefs.getInt("interfaceVibrationsScrollIntensity", 50)
        scrollLength = prefs.getInt("interfaceVibrationsScrollLength", 20)
        specialIntensity = prefs.getInt("interfaceVibrationsSpecialIntensity", 100)
        specialLength = prefs.getInt("interfaceVibrationsSpecialLength", 50)
        buttonsHeight = prefs.getFloat("interfaceLayoutHeight", 1.0f)
        buttonsMiddleWidth = prefs.getFloat("interfaceLayoutMiddleWidth", 0.2f)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        readSettings() // Read settings first to determine whether visuals are turned on

        // Set system view visibility
        requireActivity().window.statusBarColor =
            resources.getColor(if (theme) R.color.mouse_background_dark else R.color.mouse_background_light)
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
        root = view.findViewById(R.id.slides_controller_root)
        root?.setBackgroundResource(if (theme) R.color.mouse_background_dark else R.color.mouse_background_light)
        init()
    }

    /**
     * Initializes the fragment.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        root!!.post {
            // Wait for root view to get its size
            calculate()
            if (visuals) createVisuals()
        }
        if (vibrations) vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    }

    /**
     * Calculates the buttons size.
     */
    private fun calculate() {
        width = root!!.width
        height = root!!.height
        val buttonWidth = (width * ((1 - buttonsMiddleWidth) / 2)).toInt()
        val buttonHeight = (height * 0.5f).toInt()
        leftX = 0
        leftY = 0
        leftHeight = buttonHeight
        leftWidth = buttonWidth
        rightX = width - buttonWidth
        rightY = 0
        rightHeight = buttonHeight
        rightWidth = buttonWidth
        middleX = buttonWidth
        middleY = 0
        middleHeight = buttonHeight
        middleWidth = width - buttonWidth * 2
    }

    /**
     * Creates the visuals.
     */
    private fun createVisuals() {
        val horizontal = View(context)
        horizontal.setBackgroundResource(if (theme) R.color.mouse_stroke_dark else R.color.mouse_stroke_light)
        horizontal.alpha = viewIntensity
        horizontal.layoutParams = FrameLayout.LayoutParams(width, buttonsStrokeWeight)
        horizontal.y = middleHeight.toFloat()
        horizontal.x = 0f
        root!!.addView(horizontal)

    }


}