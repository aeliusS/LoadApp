package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var xTextCenter = 0.0f
    private var yTextCenter = 0.0f

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.getDimension(R.dimen.stroke_width)
        textSize = resources.getDimension(R.dimen.default_text_size)
        textAlign = Paint.Align.CENTER
        color = ResourcesCompat.getColor(resources, R.color.white, null)
    }

    private var buttonText = resources.getString(R.string.button_download)
    private var textPixelSize = textPaint.measureText(buttonText)
    private var percentageLoaded = -1.0f
    // determines the size of the loading circle
    private val radius = 0.45f
    // determines how far way the loading circle will be placed after the text
    private val circleOffset = 10f

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 25F
        color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
    }

    private var buttonRectF = RectF()
    private var arcRectF = RectF()

    // use this as a pulse to animate the circle in the button
    // https://medium.com/mindorks/android-property-animation-the-valueanimator-4ca173567cdb
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                percentageLoaded = -1.0f
                Log.d("LoadingButton", "Finished animation")
            }
        })
        addUpdateListener { updatedAnimation ->
            percentageLoaded = updatedAnimation.animatedValue as Float
            invalidate()
        }
    }

    // use the delegates observable to react to the different button states
    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { prop, old, new ->
        when (new) {
            ButtonState.Loading -> {
                buttonText = resources.getString(R.string.button_loading)
            }
            ButtonState.Completed -> {
                buttonText = resources.getString(R.string.button_download)
            }
            else -> {}
        }
        textPixelSize = textPaint.measureText(buttonText)
        contentDescription = buttonText
        updateLoadingCircleDimen()
        invalidate()
    }


    init {
        isClickable = true
        Log.d("LoadingButton", "Loaded LoadingButton class")
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (valueAnimator.isRunning) valueAnimator.end() else valueAnimator.start()
        buttonState = ButtonState.Clicked
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawButton(canvas)
        drawButtonText(canvas)
        drawLoadingCircle(canvas)
        // Log.d("LoadingButton", "Finished draw")
        // logDimensions()
    }

    private fun logDimensions() {
        Log.d("LoadingButton", "Arc: ${arcRectF.toShortString()}")
        Log.d("LoadingButton", "Button: ${buttonRectF.toShortString()}")
        Log.d("LoadingButton", "Height: $heightSize Width: $widthSize")
        Log.d("LoadingButton", "ButtonRectBottom: ${buttonRectF.bottom}")
    }

    private fun drawLoadingCircle(canvas: Canvas) {
        if (percentageLoaded == -1.0f) return
        canvas.drawArc(arcRectF, 0F, 360F * percentageLoaded, true, circlePaint)
    }

    private fun drawButton(canvas: Canvas) {
        canvas.drawRect(buttonRectF, buttonPaint)
    }

    private fun drawButtonText(canvas: Canvas) {
        canvas.drawText(buttonText, xTextCenter, yTextCenter, textPaint)
    }

    private fun updateLoadingCircleDimen() {
        val halfWidth = widthSize.toFloat() / 2
        val halfHeight = heightSize.toFloat() / 2
        arcRectF.top = halfHeight - (halfHeight * radius)
        arcRectF.bottom = halfHeight + (halfHeight * radius)

        arcRectF.left = halfWidth + (textPixelSize/2) + circleOffset
        arcRectF.right = arcRectF.left + (2 * halfHeight * radius)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xTextCenter = (w / 2).toFloat()
        yTextCenter = (h / 2).toFloat() - ((textPaint.descent() + textPaint.ascent()) / 2)

        // update the button rectangle dimensions
        buttonRectF.right = widthSize.toFloat()
        buttonRectF.bottom = heightSize.toFloat()

        updateLoadingCircleDimen()
    }

    // https://medium.com/@quiro91/custom-view-mastering-onmeasure-a0a0bb11784d
    // https://developer.android.com/training/custom-views/custom-drawing
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}