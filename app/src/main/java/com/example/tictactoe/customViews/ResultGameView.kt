package com.example.tictactoe.customViews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withSave
import com.example.tictactoe.R
import com.example.tictactoe.data.Point
import com.example.tictactoe.data.ResultGame
import kotlin.math.max

class ResultGameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val backgroundClipPath = Path()
    private val avatarClipPath = Path()

    private var avatarRadius = 0f
    private var avatarMargin = 0f
    private var backgroundCornerRadius = 0f
    private var sizeTriangle = 0f

    private lateinit var backgroundRect: RectF
    private lateinit var avatarRect: RectF

    private lateinit var center: Point
    private lateinit var topLeft: Point
    private lateinit var bottomLeft: Point
    private lateinit var bottomRight: Point

    private lateinit var topSecond: Point
    private lateinit var bottomSecond: Point
    private lateinit var leftSecond: Point
    private lateinit var rightSecond: Point

    var text = ""
        set(value) {
            field = value
            invalidate()
        }

    var avatarResId: Int = 0
        set(value) {
            field = value
            if (value != 0 && width > 0 && height > 0) {
                updateAvatarBitmap()
            }
        }

    var avatarBitmap: Bitmap? = null
        set(value) {
            field = value
            invalidate()
        }

    init {
        if (attrs != null) {
            initAttrs(attrs, defStyleAttr, defStyleRes)
        }
    }

    private fun initAttrs(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null || avatarBitmap == null) {
            return
        }

        context.withStyledAttributes(attrs, R.styleable.ResultGameView, defStyleAttr, defStyleRes) {
            text = getString(R.styleable.ResultGameView_text) ?: ""
            avatarResId =
                getResourceId(R.styleable.ResultGameView_avatar, R.drawable.profile_avatar)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight

        val desiredWidth =
            max(minWidth, MeasureSpec.getSize(widthMeasureSpec) + paddingLeft + paddingRight)
        val desiredHeight = desiredWidth

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sizeTriangle = width / 10f
        backgroundCornerRadius = width / 20f
        avatarRadius = width / 3.5f
        avatarMargin = width / 40f

        backgroundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        center = Point(width / 2f, height / 3f)
        topLeft = Point(0f, 0f)
        bottomLeft = Point(0f, height.toFloat())
        bottomRight = Point(width.toFloat(), height.toFloat())

        topSecond = Point(sizeTriangle, 0f)
        bottomSecond = Point(sizeTriangle, height.toFloat())
        leftSecond = Point(0f, height.toFloat() - sizeTriangle)
        rightSecond = Point(width.toFloat(), height.toFloat() - sizeTriangle)

        val locale = context.resources.configuration.locales[0]
        textPaint.textSize = if (locale.language == "uk") width / 8f else width / 6f

        backgroundClipPath.reset()
        backgroundClipPath.addRoundRect(
            backgroundRect, backgroundCornerRadius, backgroundCornerRadius, Path.Direction.CW
        )

        avatarClipPath.reset()
        avatarClipPath.addCircle(
            center.x, center.y, avatarRadius - avatarMargin, Path.Direction.CW
        )

        if (avatarResId != 0) {
            updateAvatarBitmap()
        }

        avatarBitmap?.let { bitmap ->
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val scale = if (bitmapWidth > 0 && bitmapHeight > 0) {
                (avatarRadius * 2) / minOf(bitmapWidth, bitmapHeight).toFloat()
            } else {
                0f
            }

            val left = center.x - (bitmapWidth * scale / 2)
            val top = center.y - (bitmapHeight * scale / 2)

            avatarRect = RectF(
                left, top, left + bitmapWidth * scale, top + bitmapHeight * scale
            )
        } ?: run {
            avatarRect = RectF(0f, 0f, 0f, 0f)
        }
    }

    private val paintMediumPurple = createPaint(R.color.medium_purple)

    private val paintRoyalPurple = createPaint(R.color.royal_purple)

    private val paintBackground = createPaint(R.color.royal_purple)

    private val paintAvatar = createPaint(R.color.dark_indigo)

    private val textPaint = Paint().apply {
        color = context.getColor(android.R.color.white)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(
            backgroundRect, backgroundCornerRadius, backgroundCornerRadius, paintBackground
        )

        canvas.withSave {
            clipPath(backgroundClipPath)

            // draw top triangle
            drawTriangleRow(
                this, center, topLeft, topSecond, sizeTriangle, true
            )

            // draw bottom triangle
            drawTriangleRow(
                this, center, bottomLeft, bottomSecond, sizeTriangle, false
            )

            // draw left triangle
            drawTriangleColumn(
                this, center, bottomLeft, leftSecond, sizeTriangle, true
            )

            // draw right triangle
            drawTriangleColumn(
                this, center, bottomRight, rightSecond, sizeTriangle, false
            )

        }

        canvas.drawText(text, width / 2f, height / 2f + avatarRadius, textPaint)

        canvas.drawCircle(center.x, center.y, avatarRadius, paintAvatar)
        canvas.withSave {
            clipPath(avatarClipPath)
            avatarBitmap?.let {
                canvas.drawBitmap(it, null, avatarRect, null)
            }
        }
    }

    fun loadResultGame(resultGame: ResultGame) {
        text = resultGame.result
        avatarBitmap = resultGame.avatarBitmap
        requestLayout()
        invalidate()
    }

    private fun createPaint(color: Int) = Paint().apply {
        this.color = context.getColor(color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private fun updateAvatarBitmap() {
        val drawable = AppCompatResources.getDrawable(context, avatarResId)
        if (drawable != null) {
            val size = (avatarRadius * 2).toInt()
            avatarBitmap = if (size > 0) {
                drawable.toBitmap(width = size, height = size)
            } else {
                drawable.toBitmap(width = 100, height = 100)
            }
        }
    }

    private fun drawTriangle(x: Point, y: Point, z: Point, paint: Paint, canvas: Canvas) {
        val path = Path()
        path.moveTo(x.x, x.y)
        path.lineTo(y.x, y.y)
        path.lineTo(z.x, z.y)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawTriangleRow(
        canvas: Canvas,
        center: Point,
        start: Point,
        second: Point,
        size: Float,
        startWithMedium: Boolean
    ) {
        var y = start
        var z = second

        for (i in 1..10) {
            val useMediumColor = (i % 2 != 0 && startWithMedium) || (i % 2 == 0 && !startWithMedium)
            val paint = if (useMediumColor) paintMediumPurple else paintRoyalPurple

            drawTriangle(center, y, z, paint, canvas)
            y = z
            z = Point(z.x + size, z.y)
        }
    }

    private fun drawTriangleColumn(
        canvas: Canvas,
        center: Point,
        start: Point,
        second: Point,
        size: Float,
        startWithMedium: Boolean
    ) {
        var y = start
        var z = second

        for (i in 1..10) {
            val useMediumColor = (i % 2 != 0 && startWithMedium) || (i % 2 == 0 && !startWithMedium)
            val paint = if (useMediumColor) paintMediumPurple else paintRoyalPurple

            drawTriangle(center, y, z, paint, canvas)
            y = z
            z = Point(z.x, z.y - size)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.resultGame = ResultGame(avatarBitmap, text)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        avatarBitmap = savedState.resultGame?.avatarBitmap
        text = savedState.resultGame?.result ?: ""
    }


    class SavedState : BaseSavedState {
        var resultGame: ResultGame? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            resultGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readParcelable(ResultGame::class.java.classLoader, ResultGame::class.java)
            } else {
                @Suppress("DEPRECATION") parcel.readParcelable(ResultGame::class.java.classLoader)
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(resultGame, flags)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return Array(size) { null }
                }
            }
        }
    }
}
