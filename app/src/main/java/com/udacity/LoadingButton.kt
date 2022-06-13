package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var xTextCenter = 0.0f
    private var yTextCenter = 0.0f

    private var buttonColor = 0
    private var loadingWidthColor = 0
    private var loadingCircleColor = 0
    private var textColor = 0

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.getDimension(R.dimen.stroke_width)
        textSize = resources.getDimension(R.dimen.default_text_size)
        textAlign = Paint.Align.CENTER
    }

    private var buttonText = resources.getString(R.string.button_download)
    private var textPixelSize = textPaint.measureText(buttonText)
    private var percentageLoaded = -1.0f

    // determines the size of the loading circle
    private val radius = 0.45f

    // determines how far way the loading circle will be placed after the text
    private val circleOffset = 10f
    private val animationDuration: Long = 1500
    private var finishedLoading = false

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var buttonRectF = RectF()
    private var loadingWidthRectF = RectF()
    private var arcRectF = RectF()

    // use this as a pulse to animate the circle in the button
    // https://medium.com/mindorks/android-property-animation-the-valueanimator-4ca173567cdb
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animationDuration
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                buttonState = ButtonState.Loading
            }
            override fun onAnimationEnd(animation: Animator?) {
                resetButton()
                Log.d("LoadingButton", "Finished animation")
                if (finishLoadingAnimation()) buttonState = ButtonState.Completed
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
            ButtonState.Loading -> buttonText = resources.getString(R.string.button_loading)
            ButtonState.Completed -> buttonText = resources.getString(R.string.button_download)
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

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColor = getColor(R.styleable.LoadingButton_baseButtonColor, 0)
            loadingWidthColor = getColor(R.styleable.LoadingButton_widthLoadingColor, 0)
            loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, Color.WHITE)
        }

        circlePaint.color = loadingCircleColor
        textPaint.color = textColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xTextCenter = (w / 2).toFloat()
        yTextCenter = (h / 2).toFloat() - ((textPaint.descent() + textPaint.ascent()) / 2)

        // update dimensions
        updateButtonDimen()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLoadingWidth(canvas)
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

    private fun drawLoadingWidth(canvas: Canvas) {
        if (percentageLoaded == -1.0f) return
        buttonPaint.color = loadingWidthColor
        loadingWidthRectF.right = widthSize.toFloat() * percentageLoaded
        canvas.drawRect(loadingWidthRectF, buttonPaint)
    }

    private fun drawButton(canvas: Canvas) {
        buttonPaint.color = buttonColor
        // limit overdraw
        if (percentageLoaded != -1.0f) buttonRectF.left = loadingWidthRectF.right
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

        arcRectF.left = halfWidth + (textPixelSize / 2) + circleOffset
        arcRectF.right = arcRectF.left + (2 * halfHeight * radius)
    }

    private fun updateButtonDimen() {
        buttonRectF.right = widthSize.toFloat()
        buttonRectF.bottom = heightSize.toFloat()

        loadingWidthRectF.bottom = heightSize.toFloat()
    }

    private fun resetButton() {
        percentageLoaded = -1.0f
        buttonRectF.left = 0.0f
        valueAnimator.duration = animationDuration
        isClickable = true
    }

    /**
     * if download finished, do nothing
     * else if not, reset with a different duration and start again
     * returns true when the loading animation is meant to be finished
    **/
    private fun finishLoadingAnimation(): Boolean {
        if (finishedLoading) return true

        valueAnimator.duration = (valueAnimator.duration * 1.5).toLong()
        valueAnimator.start()
        return false
    }

    fun setButtonLoading() {
        isClickable = false
        finishedLoading = false
        if (!valueAnimator.isRunning) valueAnimator.start()
    }

    fun setButtonCompletedDownload() {
        finishedLoading = true
        isClickable = true
        // speed up the animation to completion when the download is finished
        if (valueAnimator.isRunning) {
            valueAnimator.pause()
            valueAnimator.duration = (animationDuration * 0.75).toLong()
            valueAnimator.resume()
        }
    }

}