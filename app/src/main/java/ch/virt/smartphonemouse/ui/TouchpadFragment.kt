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
import ch.virt.smartphonemouse.mouse.MouseInputs
import ch.virt.smartphonemouse.ui.mouse.MouseUsageDialog

/**
 * This fragment represents the mouse interface the user uses to input button clicks.
 */
class TouchpadFragment
/**
 * Creates a Mouse Fragment.
 *
 * @param mouse the movement to attach to
 */(private val mouse: MouseInputs?) :
    Fragment(R.layout.fragment_mouse) {
    private var root: RelativeLayout? = null
    private var width = 0
    private var height = 0
    private var theme = false // false = light, true = dark
    private var xDown = 0f
    private var yDown = 0f

    // Feedback
    private var visuals = false
    private var viewIntensity = 0f
    private var vibrations = false
    private var buttonIntensity = 0
    private var buttonLength = 0
    private var leftView: View? = null
    private var rightView: View? = null
    private var vibrator: Vibrator? = null

    private var left = false
    private var right = false




    /**
     * Reads the settings for the fragment from the preferences.
     */
    private fun readSettings() {
        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        theme = prefs!!.getString("interfaceTheme", "dark") == "dark"
        visuals = prefs.getBoolean("interfaceVisualsEnable", true)
        viewIntensity = prefs.getFloat("interfaceVisualsIntensity", 0.5f)
        vibrations = prefs.getBoolean("interfaceVibrationsEnable", true)
        buttonIntensity = prefs.getInt("interfaceVibrationsButtonIntensity", 100)
        buttonLength = prefs.getInt("interfaceVibrationsButtonLength", 30)
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
        root = view.findViewById(R.id.mouse_root)
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
            if (visuals) createVisuals()
        }
        root!!.setOnTouchListener { v: View?, event: MotionEvent -> viewTouched(event) }
        if (vibrations) vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (context?.let { PreferenceManager.getDefaultSharedPreferences(it).getBoolean("showUsage", true) } == true) {
            mouse!!.stop()
            val dialog = MouseUsageDialog(object : MouseUsageDialog.UsageFinishedListener {
                override fun finished() {
                    mouse.start()
                }
            })
            dialog.show(parentFragmentManager, null)
        }
    }


    /**
     * Creates the visuals.
     */
    private fun createVisuals() {
        width = root!!.width
        height = root!!.height
        leftView = View(context)
        leftView!!.setBackgroundResource(if (theme) R.color.mouse_pressed_dark else R.color.mouse_pressed_light)
        leftView!!.alpha = viewIntensity
        leftView!!.layoutParams =
            FrameLayout.LayoutParams(width, height/2)
        leftView!!.x = 0F
        leftView!!.y = 0F
        root!!.addView(leftView)
        rightView = View(context)
        rightView!!.setBackgroundResource(if (theme) R.color.mouse_pressed_dark else R.color.mouse_pressed_light)
        rightView!!.alpha = viewIntensity
        rightView!!.layoutParams =
            FrameLayout.LayoutParams(width, height/2)
        rightView!!.x = 0F
        rightView!!.y = (height/2).toFloat()
        root!!.addView(rightView)
        leftView!!.visibility = View.INVISIBLE
        rightView!!.visibility = View.INVISIBLE
    }

    /**
     * Processes all touch events.
     *
     * @param event touch event
     * @return whether used
     */
    private fun viewTouched(event: MotionEvent): Boolean {
        // Temporary Variables
        var left = false
        var right = false

        // Check whether a pointer is on a button, and if, check whether it is currently releasing or not
        for (i in 0 until event.pointerCount) {
            if (within(
                    event.getX(i),
                    event.getY(i),
                    0,
                    0,
                    width,
                    height/2
                )
            ) { // Left Mouse Button
                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_MOVE && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i) {
                    left = true
                    xDown = event.x
                    yDown = event.y

                }

            }
            if (within(
                    event.getX(i),
                    event.getY(i),
                    0,
                    height/2,
                    width,
                    height/2
                )
            ) { // Right Mouse Button
                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_MOVE && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i) {
                    right = true
                    xDown = event.x
                    yDown = event.y
                }
            }
            if (within(
                    event.getX(i),
                    event.getY(i),
                    0,
                    0,
                    width,
                    height
                )
            ){ // single drag
                if (event.actionIndex == i && event.actionMasked == MotionEvent.ACTION_MOVE || event.actionIndex != i) {
                    left = this.left
                    right = this.right
                    var distX = event.x - xDown
                    var distY = event.y - yDown
                    mouse!!.changeXPosition(distY)
                    mouse!!.changeYPosition(-distX)
                    xDown = event.x
                    yDown = event.y


                }
            }
        }

        // Update Feedback
        if (this.left != left) {
            vibrate(buttonLength, buttonIntensity)
//            setVisibility(leftView, left)
        }
        if (this.right != right) {
            vibrate(buttonLength, buttonIntensity)
//            setVisibility(rightView, right)
        }

        if (this.left != left) mouse!!.setLeftButton(left)
        if (this.right != right) mouse!!.setRightButton(right)

        // Update self
        this.left = left
        this.right = right
        return true
    }

    /**
     * Vibrates the device if the vibrations are enabled.
     *
     * @param length    length of the vibration
     * @param intensity intensity of the vibration
     */
    private fun vibrate(length: Int, intensity: Int) {
        if (vibrations) vibrator!!.vibrate(
            VibrationEffect.createOneShot(
                length.toLong(),
                intensity
            )
        )
    }

    /**
     * Sets the visibility of a view if the visuals are enabled.
     *
     * @param view    view to set visibility for
     * @param visible whether the view is visible
     */
    private fun setVisibility(view: View?, visible: Boolean) {
        if (!visuals) return
        if (visible) requireView().visibility = View.VISIBLE else requireView().visibility = View.INVISIBLE
    }

    companion object {
        /**
         * Checks whether certain coordinates are within a boundary.
         *
         * @param touchX x coordinate
         * @param touchY y coordinate
         * @param x      x coordinate of the boundary
         * @param y      y coordinate of the boundary
         * @param width  width of the boundary
         * @param height height of the boundary
         * @return whether it is inside
         */
        private fun within(
            touchX: Float,
            touchY: Float,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ): Boolean {
            return touchX > x && touchX < x + width && touchY > y && touchY < y + height
        }
    }
}