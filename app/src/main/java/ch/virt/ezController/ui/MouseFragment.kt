package ch.virt.ezController.ui

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
import ch.virt.ezController.R
import ch.virt.ezController.mouse.MouseInputs

private const val TAG = "MouseFragment"
// This fragment represents the mouse fragment.
class MouseFragment
(private val mouse: MouseInputs?) :
    Fragment(R.layout.fragment_mouse) {
    private var root: RelativeLayout? = null
    private var width = 0
    private var height = 0

    // Feedback
    private var buttonsStrokeWeight = 4
    private var viewIntensity = 0.5f
    private var buttonIntensity = 100
    private var buttonLength = 30
    private var scrollIntensity = 50
    private var scrollLength = 20
    private var specialIntensity = 100
    private var specialLength = 50
    private var leftView: View? = null
    private var rightView: View? = null
    private var middleView: View? = null
    private var vibrator: Vibrator? = null

    // Buttons
    private var buttonsHeight = 0.3f
    private var buttonsMiddleWidth = 0.2f
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
    var left = false
    var right = false
    var middle = false

    // Middle Specific
    private var middleClickWait = 300
    private var scrollThreshold = 50
    private var middleStart = 0
    private var middleStartTime: Long = 0
    private var middleDecided = false
    private var middleScrolling = false

    @SuppressLint("ClickableViewAccessibility", "ServiceCast")
    private fun init() {
        root!!.post {
            calculate()
            createVisuals()
        }
        root!!.setOnTouchListener { v: View?, event: MotionEvent -> viewTouched(event) }
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor =
            resources.getColor(R.color.mouse_background_dark)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.mandatorySystemGestures())
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.systemGestures())
            requireActivity().window.insetsController!!.hide(WindowInsets.Type.navigationBars())
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        requireActivity().window.statusBarColor =
            resources.getColor(R.color.design_default_color_primary_dark)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
        root?.setBackgroundResource(R.color.mouse_background_dark)
        init()
    }

    // Calculates the buttons size.
    private fun calculate() {
        width = root!!.width
        height = root!!.height
        val buttonWidth = (width * ((1 - buttonsMiddleWidth) / 2)).toInt()
        val buttonHeight = (height * buttonsHeight).toInt()
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

    // Creates the visuals.
    private fun createVisuals() {
        val horizontal = View(context)
        horizontal.setBackgroundResource(R.color.mouse_stroke_dark)
        horizontal.alpha = viewIntensity
        horizontal.layoutParams = FrameLayout.LayoutParams(width, buttonsStrokeWeight)
        horizontal.y = middleHeight.toFloat()
        horizontal.x = 0f
        root!!.addView(horizontal)

        val verticalLeft = View(context)
        verticalLeft.setBackgroundResource(R.color.mouse_stroke_dark)
        verticalLeft.alpha = viewIntensity
        verticalLeft.layoutParams = FrameLayout.LayoutParams(buttonsStrokeWeight, leftHeight)
        verticalLeft.x = (leftWidth - buttonsStrokeWeight / 2).toFloat()
        verticalLeft.y = leftY.toFloat()
        root!!.addView(verticalLeft)

        val verticalRight = View(context)
        verticalRight.setBackgroundResource(R.color.mouse_stroke_dark)
        verticalRight.alpha = viewIntensity
        verticalRight.layoutParams = FrameLayout.LayoutParams(buttonsStrokeWeight, rightHeight)
        verticalRight.x = (rightX - buttonsStrokeWeight / 2).toFloat()
        verticalRight.y = rightY.toFloat()
        root!!.addView(verticalRight)

        leftView = View(context)
        leftView!!.setBackgroundResource(R.color.mouse_pressed_dark)
        leftView!!.alpha = viewIntensity
        leftView!!.layoutParams =
            FrameLayout.LayoutParams(leftWidth - buttonsStrokeWeight / 2, leftHeight)
        leftView!!.x = leftX.toFloat()
        leftView!!.y = leftY.toFloat()
        root!!.addView(leftView)
        rightView = View(context)
        rightView!!.setBackgroundResource(R.color.mouse_pressed_dark)
        rightView!!.alpha = viewIntensity
        rightView!!.layoutParams =
            FrameLayout.LayoutParams(rightWidth - buttonsStrokeWeight / 2, rightHeight)
        rightView!!.x = (rightX + buttonsStrokeWeight / 2).toFloat()
        rightView!!.y = rightY.toFloat()
        root!!.addView(rightView)
        middleView = View(context)
        middleView!!.setBackgroundResource(R.color.mouse_pressed_dark)
        middleView!!.alpha = viewIntensity
        middleView!!.layoutParams =
            FrameLayout.LayoutParams(middleWidth - buttonsStrokeWeight, middleHeight)
        middleView!!.x = (middleX + buttonsStrokeWeight / 2).toFloat()
        middleView!!.y = middleY.toFloat()
        root!!.addView(middleView)
        leftView!!.visibility = View.INVISIBLE
        rightView!!.visibility = View.INVISIBLE
        middleView!!.visibility = View.INVISIBLE
    }

    private fun viewTouched(event: MotionEvent): Boolean {
        // Temporary Variables
        var left = false
        var right = false
        var middle = false
        if(event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP){
            left = this.left
            right = this.right
            middle = this.middle
        }

        for (i in 0 until event.pointerCount) {
            if (within(
                    event.getX(i),
                    event.getY(i),
                    leftX,
                    leftY,
                    leftWidth,
                    leftHeight
                )
            ) { // Left Mouse Button
                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i)
                    if(!right && !middle)
                        left = true
            }
            if (within(
                    event.getX(i),
                    event.getY(i),
                    rightX,
                    rightY,
                    rightWidth,
                    rightHeight
                )
            ) { // Right Mouse Button
                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i)
                    if(!left && !middle)
                        right = true
            }
            if (within(
                    event.getX(i),
                    event.getY(i),
                    middleX,
                    middleY,
                    middleWidth,
                    middleHeight
                )
            ) { // Middle Mouse Button
                if (event.actionIndex == i && event.actionMasked != MotionEvent.ACTION_POINTER_UP && event.actionMasked != MotionEvent.ACTION_UP || event.actionIndex != i)
                    if(!left && !right)
                        middle = true
                if (!this.middle && middle) {
                    middleStart = event.getY(i).toInt()
                    middleStartTime = System.currentTimeMillis()
                    middleDecided = false
                } else if (middle) {
                    if (middleStart - event.getY(i) > scrollThreshold && (!middleDecided || middleScrolling)) { // Scroll up
                        mouse!!.changeWheelPosition(1)
                        middleStart -= scrollThreshold
                        middleDecided = true
                        middleScrolling = true
                        requireView().visibility = View.VISIBLE
                        vibrate(scrollLength, scrollIntensity)
                    } else if (middleStart - event.getY(i) < -scrollThreshold && (!middleDecided || middleScrolling)) { // Scroll down
                        mouse!!.changeWheelPosition(-1)
                        middleStart += scrollThreshold
                        middleDecided = true
                        middleScrolling = true
                        requireView().visibility = View.VISIBLE
                        vibrate(scrollLength, scrollIntensity)
                    } else { // Click
                        if (System.currentTimeMillis() - middleStartTime > middleClickWait && !middleDecided) {
                            mouse!!.setMiddleButton(true)
                            middleDecided = true
                            middleScrolling = false
                            requireView().visibility = View.VISIBLE
                            vibrate(specialLength, specialIntensity)
                        }
                    }
                }
            }
        }

        if (this.left != left) {
            vibrate(buttonLength, buttonIntensity)
        }
        if (this.right != right) {
            vibrate(buttonLength, buttonIntensity)
        }
        if (this.middle != middle) {
            if (!middle && middleDecided && !middleScrolling) vibrate(buttonLength, buttonIntensity)
        }

        if (this.middle != middle && !middle && middleDecided && !middleScrolling) mouse!!.setMiddleButton(
            false
        )
        if (this.left != left) mouse!!.setLeftButton(left)
        if (this.right != right) mouse!!.setRightButton(right)

        Log.d(TAG, "Mouse status: " + left + " " + right + " " + middle)
        // Update self
        this.left = left
        this.right = right
        this.middle = middle
        return true
    }
    //If the vibrations of the device are enabled, the device vibrates
    private fun vibrate(length: Int, intensity: Int) {
        vibrator!!.vibrate(
            VibrationEffect.createOneShot(
                length.toLong(),
                intensity
            )
        )
    }
    //check if the input is within a certain range
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