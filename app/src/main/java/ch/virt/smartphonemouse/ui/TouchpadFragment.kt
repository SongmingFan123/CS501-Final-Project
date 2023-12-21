package ch.virt.smartphonemouse.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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
import java.lang.Math.abs

/**
 * This fragment represents the Touchpad interface the user uses to input clicks and drags
 */
class TouchpadFragment
/**
 * Creates a Touchpad Fragment.
 *
 * @param mouse the movement to attach to
 */(private val mouse: MouseInputs?) :
    Fragment(R.layout.fragment_touchpad) {
    private var root: RelativeLayout? = null
    private var width = 0
    private var height = 0
    private var theme = true // false = light, true = dark
    private var xDownI = 0f
    private var yDownI = 0f
    private var xDownJ = 0f
    private var yDownJ = 0f
    private var downTimeI = 0
    private var downTimeJ = 0
    private var scrollUpI = false
    private var scrollUpJ = false
    private var scrollDownI = false
    private var scrollDownJ = false
    private var isMoving = false
    private var isDragging = false
    private val LONG_HOLD_TIME_BOUND = 300
    private var oneUp = false
    private val TAG = "Touchpad fragment"


    // Feedback
    private var visuals = false
    private var viewIntensity = 0.5f
    private var vibrations = true
    private var buttonIntensity = 100
    private var buttonLength = 100
    private var touchpadView: View? = null
    private var vibrator: Vibrator? = null





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
        root = view.findViewById(R.id.touchpad_root)
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
        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            if(event.actionIndex == 0){
                downTimeI = System.currentTimeMillis().toInt()
                xDownI = event.x
                yDownI = event.y
            }else if(event.actionIndex == 1){
                downTimeJ = System.currentTimeMillis().toInt()
                xDownJ = event.x
                yDownJ = event.y
            }
        }

        if(event.pointerCount == 1) {
            if(event.actionMasked == MotionEvent.ACTION_MOVE ){
                if(!isMoving && !isDragging){
                    if(System.currentTimeMillis().toInt() - downTimeI <= LONG_HOLD_TIME_BOUND){
                        isMoving = true
                        downTimeI = 0
                    }else{
                        isDragging = true
                        mouse!!.setLeftButton(true)
                        vibrate(buttonLength, buttonIntensity)
                        downTimeI = 0
                    }
                }
                mouse!!.changeXPosition(event.y - yDownI)
                mouse!!.changeYPosition(-(event.x - xDownI))
                xDownI = event.x
                yDownI = event.y
            }
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                if (System.currentTimeMillis().toInt() - downTimeI <= LONG_HOLD_TIME_BOUND || System.currentTimeMillis().toInt() - downTimeJ <= LONG_HOLD_TIME_BOUND) {
                    if(oneUp){
                        mouse!!.setRightButton(true)
                        Thread.sleep(200)
                        mouse!!.setRightButton(false)
                        vibrate(buttonLength, buttonIntensity)
                    }else {
                        mouse!!.setLeftButton(true)
                        Thread.sleep(200)
                        mouse!!.setLeftButton(false)
                        vibrate(buttonLength, buttonIntensity)
                    }
                }
                if (isDragging) {
                    mouse!!.setLeftButton(false)
                }
                isMoving = false
                isDragging = false
                downTimeI = 0
                oneUp = false
                return true
            }
        }else if(event.pointerCount == 2) {
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                if (System.currentTimeMillis().toInt() - downTimeI <= LONG_HOLD_TIME_BOUND || System.currentTimeMillis().toInt() - downTimeJ <= LONG_HOLD_TIME_BOUND) {
                    oneUp = true
                    isMoving = false
                    isDragging = false
                    downTimeI = 0
                }
            }
            if(event.actionMasked == MotionEvent.ACTION_MOVE ){
                if(event.actionIndex == 0){
                    scrollUpI = (event.x - xDownI) > 30
                    scrollDownI = (xDownI - event.x) > 30
                    Log.d(TAG, event.x.toString())
                    Log.d(TAG, xDownI.toString())
                    if(scrollUpI || scrollDownI){
                        xDownI = event.x
                        yDownI = event.y
                    }
                }
                if(event.actionIndex == 1){
                    scrollUpJ = (event.x - xDownJ) > 30
                    scrollDownJ = (xDownJ - event.x) > 30
//                    Log.d(TAG, scrollUpJ.toString())
//                    Log.d(TAG, scrollDownJ.toString())
                    if(scrollUpJ||scrollDownJ){
                        xDownJ = event.x
                        yDownJ = event.y
                    }
                }
                if(scrollUpI && !scrollUpJ){
                    mouse!!.changeWheelPosition(1)
                    isMoving = true
                }
                if(scrollDownI && !scrollDownJ){
                    mouse!!.changeWheelPosition(-1)
                    isMoving = true
                }
            }
        }
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