package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var xTextCenter = 0.0f
    private var yTextCenter = 0.0f

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.strokeWidth)
        textSize = resources.getDimension(R.dimen.default_text_size)
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
    }

    // use this as a pulse to animate the circle in the button
    // https://medium.com/mindorks/android-property-animation-the-valueanimator-4ca173567cdb
    private val valueAnimator = ValueAnimator()

    // use the delegates observable to react to the different button states
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { prop, old, new ->

    }


    init {
        Log.d("LoadingButton", "Loaded LoadingButton class")
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // TODO: Draw rectangle
        canvas.drawText(
            context.getString(R.string.button_download),
            xTextCenter,
            yTextCenter,
            paint
        )
        Log.d("LoadingButton", "Finished draw")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xTextCenter = (w/2).toFloat()
        yTextCenter = (h/2).toFloat() - ((paint.descent() + paint.ascent()) /2)
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