package com.chigo.tappgospinwheel.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.chigo.tappgospinwheel.model.RotationConfig
import kotlin.math.min
import kotlin.random.Random

class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Assets
    private var bgBitmap: Bitmap? = null
    private var wheelBitmap: Bitmap? = null
    private var frameBitmap: Bitmap? = null
    private var spinBtnBitmap: Bitmap? = null

    // State

    private var currentRotation = 0f
    private var isSpinning = false
    private var isSpinEnabled = true
    private var spinBtnAlpha = 255

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val matrix = Matrix()
    private var rotationConfig = RotationConfig()

    // Callbacks

    var onSpinComplete: ((finalDegrees: Float) -> Unit)? = null
    var onSpinStart: (() -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
    }

    // Public API
    fun setAssets(bg: Bitmap?, wheel: Bitmap?, frame: Bitmap?, spinBtn: Bitmap?) {
        bgBitmap = bg
        wheelBitmap = wheel
        frameBitmap = frame
        spinBtnBitmap = spinBtn
        invalidate()
    }

    fun setRotationConfig(config: RotationConfig) {
        rotationConfig = config
    }

    fun setSpinEnabled(enabled: Boolean) {
        isSpinEnabled = enabled
        spinBtnAlpha = if (enabled) 255 else 128
        invalidate()
    }

    fun spin() {
        if (isSpinning) return
        isSpinning = true
        onSpinStart?.invoke()

        val spins = Random.nextInt(rotationConfig.minimumSpins, rotationConfig.maximumSpins + 1)
        val extraDegrees = Random.nextFloat() * 360f
        val totalRotation = (spins * 360f) + extraDegrees
        var lastValue = 0f

        ValueAnimator.ofFloat(0f, totalRotation).apply {
            duration = rotationConfig.duration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                val current = va.animatedValue as Float
                val delta = current - lastValue
                lastValue = current
                currentRotation = (currentRotation + delta) % 360f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isSpinning = false
                    onSpinComplete?.invoke(currentRotation)
                }
            })
            start()
        }
    }

    // DrawingS
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val size = min(w, h)

        // Background
        bgBitmap?.let {
            canvas.drawBitmap(
                Bitmap.createScaledBitmap(it, width, height, true), 0f, 0f, paint
            )
        }

        // Wheel,  rotates on spin
        wheelBitmap?.let {
            val wheelSize = size * 0.85f
            val scaled = Bitmap.createScaledBitmap(it, wheelSize.toInt(), wheelSize.toInt(), true)
            matrix.reset()
            matrix.postTranslate(-wheelSize / 2f, -wheelSize / 2f)
            matrix.postRotate(currentRotation)
            matrix.postTranslate(cx, cy)
            canvas.drawBitmap(scaled, matrix, paint)
        }

        // Frame
        frameBitmap?.let {
            val frameSize = size * 0.90f
            val scaled = Bitmap.createScaledBitmap(it, frameSize.toInt(), frameSize.toInt(), true)
            canvas.drawBitmap(scaled, cx - frameSize / 2f, cy - frameSize / 2f, paint)
        }

        // Spin button, disabled during cooldwn
        spinBtnBitmap?.let {
            val btnSize = size * 0.28f
            val scaled = Bitmap.createScaledBitmap(it, btnSize.toInt(), btnSize.toInt(), true)
            paint.alpha = spinBtnAlpha
            canvas.drawBitmap(scaled, cx - btnSize / 2f, cy - btnSize / 2f, paint)
            paint.alpha = 255 // reset
        }
    }

    //Touch
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && !isSpinning && isSpinEnabled) {
            if (isSpinButtonTapped(event.x, event.y)) {
                spin()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isSpinButtonTapped(x: Float, y: Float): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val btnRadius = (min(width, height) * 0.28f) / 2f
        val dx = x - cx
        val dy = y - cy
        return (dx * dx + dy * dy) <= (btnRadius * btnRadius)
    }
}