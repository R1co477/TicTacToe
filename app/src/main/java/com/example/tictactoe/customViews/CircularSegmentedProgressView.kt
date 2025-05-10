package com.example.tictactoe.customViews

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.example.tictactoe.R
import kotlin.math.min

class CircularSegmentedProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var segmentColor = Color.GREEN
    private var segmentWidth = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        DEFAULT_SEGMENT_WIDTH_IN_DP,
        context.resources.displayMetrics
    )
    private var circleBounds = RectF(0f, 0f, 0f, 0f)
    private val segmentPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        strokeWidth = segmentWidth
        isAntiAlias = true
    }
    private var animator: ValueAnimator? = null

    init {
        initAttr(attrs, defStyleAttr, defStyleRes)
        setupAnimator()
    }

    private fun initAttr(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null) return

        context.withStyledAttributes(
            attrs,
            R.styleable.CircularSegmentedProgressView,
            defStyleAttr,
            defStyleRes
        ) {
            segmentColor = getColor(
                R.styleable.CircularSegmentedProgressView_color,
                ContextCompat.getColor(context, R.color.emerald_green)
            )
            segmentWidth = getDimension(R.styleable.CircularSegmentedProgressView_segment_size, segmentWidth)
        }
        segmentPaint.apply {
            color = segmentColor
            strokeWidth = segmentWidth
        }
    }

    private fun setupAnimator() {
        animator = ObjectAnimator.ofFloat(this, ROTATION, 0f, 360f).apply {
            duration = 2000L
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size =
            min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleBounds = RectF(
            segmentWidth,
            segmentWidth,
            height.toFloat() - segmentWidth,
            width.toFloat() - segmentWidth
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until 360 step 30) {
            if (i % 4 == 0) {
                canvas.drawArc(circleBounds, i.toFloat(), 30f, false, segmentPaint)
            }
        }
    }

    companion object {
        const val DEFAULT_SEGMENT_WIDTH_IN_DP = 6f
    }
}