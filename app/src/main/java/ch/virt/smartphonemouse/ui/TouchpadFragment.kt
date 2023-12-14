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
    private var xDownI = 0f
    private var yDownI = 0f
    private var xDownJ = 0f
    private var yDownJ = 0f
    private var downTimeI = 0
    private var downTimeJ = 0
    private var isDownI = false
    private var isDownJ = false
    private var isMoving = false
    private val LONG_HOLD_TIME_BOUND = 500


    // Feedback
    private var visuals = false
    private var viewIntensity = 0f
    private var vibrations = false
    private var buttonIntensity = 0
    private var buttonLength = 0
    private var touchpadView: View? = null
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
        buttonIntensity = prefs.getInt("interfaceVibrationsButtonIntensity", 100)
        buttonLength = prefs.getInt("interfaceVibrationsButtonLength", 30)
        viewIntensity = prefs.getFloat("interfaceVisualsIntensity", 0.5f)
        vibrations = prefs.getBoolean("interfaceVibrationsEnable", true)

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
        touchpadView = View(context)
        touchpadView!!.setBackgroundResource(if (theme) R.color.mouse_pressed_dark else R.color.mouse_pressed_light)
        touchpadView!!.alpha = viewIntensity
        touchpadView!!.layoutParams =
            FrameLayout.LayoutParams(width, height)
        touchpadView!!.x = 0F
        touchpadView!!.y = 0F
        root!!.addView(touchpadView)
    }

    /**
     * Processes all touch events.
     *
     * @param event touch event
     * @return whether used
     */
    private fun viewTouched(event: MotionEvent): Boolean {
        if(event.pointerCount == 1){
            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                downTimeI = System.currentTimeMillis().toInt()
                isDownI = true
                xDownI = event.x
                yDownI = event.y
            }
            if(event.actionMasked == MotionEvent.ACTION_MOVE){
                if(!isMoving){
                    if(System.currentTimeMillis().toInt() - downTimeI <= LONG_HOLD_TIME_BOUND){
                        isMoving = true
                        downTimeI = 0
                    }else{
                        mouse!!.setLeftButton(true)
                        vibrate(buttonLength, buttonIntensity)
                    }
                }
                mouse!!.changeXPosition(event.y - yDownI)
                mouse!!.changeYPosition(-(event.x - xDownI))
                xDownI = event.x
                yDownI = event.y
            }
            if(event.actionMasked ==  MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP){
                if(System.currentTimeMillis().toInt() - downTimeI <= LONG_HOLD_TIME_BOUND){
                    mouse!!.setLeftButton(true)
                    vibrate(buttonLength, buttonIntensity)
                    mouse!!.setLeftButton(false)
                }
                if(!isMoving){
                    mouse!!.setLeftButton(false)
                }
            }
        }

//        // Check whether a pointer is on a button, and if, check whether it is currently releasing or not
//        for (i in 0 until event.pointerCount) {
//            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
//                left = true
//                downTimeI = System.currentTimeMillis().toInt()
//                xDownI = event.x
//                yDownI = event.y
//
//            }
//            for(j in 0 until event.pointerCount){
//                if(i == j)continue
//
//            }
//
//            if (within(
//                    event.getX(i),
//                    event.getY(i),
//                    0,
//                    height/2,
//                    width,
//                    height/2
//                )
//            ) { // Right Mouse Button
//                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_MOVE && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i) {
//                    right = true
//                    downTimeI = System.currentTimeMillis().toInt()
//                    xDownI = event.x
//                    yDownI = event.y
//                }
//            }
//            if (within(
//                    event.getX(i),
//                    event.getY(i),
//                    0,
//                    0,
//                    width,
//                    height
//                )
//            ){ // single drag
//                if (event.actionIndex == i && event.actionMasked == MotionEvent.ACTION_MOVE || event.actionIndex != i) {
//                    left = this.left
//                    right = this.right
//                    var distX = event.x - xDownI
//                    var distY = event.y - yDownI
//                    mouse!!.changeXPosition(distY)
//                    mouse!!.changeYPosition(-distX)
//                    xDownI = event.x
//                    yDownI = event.y
//
//
//                }
//            }
//        }
//
//        // Update Feedback
//        if (this.left != left) {
//            vibrate(buttonLength, buttonIntensity)
//        }
//        if (this.right != right) {
//            vibrate(buttonLength, buttonIntensity)
//        }
//
//        if (this.left != left) mouse!!.setLeftButton(left)
//        if (this.right != right) mouse!!.setRightButton(right)
//
//        // Update self
//        this.left = left
//        this.right = right
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


}