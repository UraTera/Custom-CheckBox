package com.tera.custom_checkbox

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes


typealias OnCheckedChangeListener = (view: CheckBox, isChecked: Boolean) -> Unit

class CheckBox
    (
    context: Context,
    attrs: AttributeSet?,
    defStyleRes: Int
) : View(context, attrs, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    companion object {

        const val OFFSET = 41f
        const val CHECKED_COLOR = -11729768
        const val UNCHECKED_COLOR = -11513776
        const val BUTTON_SIZE = 44f
        const val ICON_UNCHECKED_COLOR = -3356210

        const val RIPPLE_ALPHA = 80
        const val RIPPLE_COLOR = -7235924
        const val RIPPLE_RADIUS = 54f

        const val TEXT_COLOR = Color.BLACK
        const val TEXT_SIZE = 14f
        const val TICK_CHECKED_COLOR = Color.WHITE
        const val TICK_UNCHECKED_COLOR = -3356210
        const val LINE_WIDTH = 5f

    }

    private var mListener: OnCheckedChangeListener? = null

    private val mPaintButton = Paint()
    private val mPaintText = Paint()
    private val mPaintTick = Paint()
    private val mPaintRipple = Paint()

    private val mPathRect = Path()
    private val mPathTick = Path()

    private var mButtonSize = BUTTON_SIZE
    private var mRadius = mButtonSize / 2f
    private val mLineWidth = LINE_WIDTH
    private var mOffsetHor = OFFSET
    private var mOffsetVert = OFFSET
    private var mXC = 0f
    private var mYC = 0f
    private var mXT = 0f
    private var mColorFilter: ColorFilter? = null
    private var mFieldText = 0f

    private var mRippleMax = RIPPLE_RADIUS // 56
    private var mRippleRadius = 0f
    private var mRippleStart = mButtonSize / 2
    private var mRippleStroke = mRippleMax - mRippleStart
    private var mAlpha = RIPPLE_ALPHA // 80
    private var mKeyDown = false

    // Атрибуты
    private var mChecked = false
    private var mGravity = 0
    private var mCheckStyle = 0

    private var mCheckColor = 0
    private var mUncheckColor = 0

    private var mIconChecked = 0
    private var mIconUncheck = 0
    private var mIconUncheckColor = 0
    private var mRippleColor = 0

    private var mText: String? = null
    private var mTextColor = 0
    private var mTextSize = 0f

    private var mTickCheckColor = 0
    private var mTickUncheckColor = 0


    init {
        context.withStyledAttributes(attrs, R.styleable.CheckBox) {
            mChecked = getBoolean(R.styleable.CheckBox_check_checked, false)
            mGravity = getInt(R.styleable.CheckBox_check_gravity, 0)
            mCheckStyle = getInt(R.styleable.CheckBox_check_style, 0)

            mCheckColor = getColor(R.styleable.CheckBox_button_checkedColor, CHECKED_COLOR)
            mUncheckColor = getColor(R.styleable.CheckBox_button_uncheckedColor, UNCHECKED_COLOR)
            mIconUncheckColor =
                getColor(R.styleable.CheckBox_icon_uncheckedColor, ICON_UNCHECKED_COLOR)

            mTickCheckColor = getColor(R.styleable.CheckBox_tick_checkedColor, TICK_CHECKED_COLOR)
            mTickUncheckColor =
                getColor(R.styleable.CheckBox_tick_uncheckedColor, TICK_UNCHECKED_COLOR)

            mIconChecked = getResourceId(R.styleable.CheckBox_check_icon_Checked, 0)
            mIconUncheck = getResourceId(R.styleable.CheckBox_check_icon_Unchecked, 0)

            mRippleColor = getColor(R.styleable.CheckBox_ripple_Color, RIPPLE_COLOR)

            mText = getString(R.styleable.CheckBox_check_text)
            mTextColor = getColor(R.styleable.CheckBox_check_textColor, TEXT_COLOR)
            mTextSize = getDimension(R.styleable.CheckBox_check_textSize, spToPx())
        }

        initPaint()
        initParams()
    }

    private fun spToPx(): Float {
        val sp = TEXT_SIZE
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun initPaint() {
        val radius = 5.0f
        val cornerPathEffect = CornerPathEffect(radius)

        mPaintButton.color = mUncheckColor
        mPaintButton.style = Paint.Style.STROKE
        mPaintButton.strokeWidth = mLineWidth
        mPaintButton.pathEffect = cornerPathEffect

        mPaintTick.color = mTickCheckColor
        mPaintTick.style = Paint.Style.STROKE
        mPaintTick.strokeWidth = mLineWidth

        mPaintText.color = mTextColor
        mPaintText.textSize = mTextSize

        mPaintRipple.color = mRippleColor
        mPaintRipple.alpha = mAlpha
    }

    private fun initParams() {

        if (mCheckStyle == 3 && mIconUncheck == 0)
            mColorFilter = getColorFilter()

        if (mText != null && mText!!.isNotEmpty()) {
            mFieldText = mPaintText.measureText(mText)
        }
    }

    private fun getColorFilter(): ColorFilter {
        val color = mIconUncheckColor

        val red = (color and 0xFF0000) / 0xFFFF
        val green = (color and 0xFF00) / 0xFF
        val blue = color and 0xFF

        val matrix = floatArrayOf(
            0f, 0f, 0f, 0f, red.toFloat(),
            0f, 0f, 0f, 0f, green.toFloat(),
            0f, 0f, 0f, 0f, blue.toFloat(),
            0f, 0f, 0f, 1f, 0f
        )
        return ColorMatrixColorFilter(matrix)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawRipple(canvas)

        when (mCheckStyle) {
            0 -> drawRect(canvas)
            1 -> drawCircle(canvas)
            2 -> drawTick(canvas)
            3 -> {
                if (mIconChecked != 0)
                    drawIcon(canvas)
            }
        }
        if (mText != null) {
            drawText(canvas)
        }
    }

    private fun drawRect(canvas: Canvas) {
        if (mChecked) {
            mPaintButton.color = mCheckColor
            mPaintButton.style = Paint.Style.FILL_AND_STROKE
            canvas.drawPath(mPathRect, mPaintButton)
            canvas.drawPath(mPathTick, mPaintTick)
        } else {
            mPaintButton.color = mUncheckColor
            mPaintButton.style = Paint.Style.STROKE
            canvas.drawPath(mPathRect, mPaintButton)
        }
    }

    private fun drawCircle(canvas: Canvas) {
        if (mChecked) {
            mPaintButton.color = mCheckColor
            mPaintButton.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle(mXC, mYC, mRadius, mPaintButton)
            canvas.drawPath(mPathTick, mPaintTick)
        } else {
            mPaintButton.color = mUncheckColor
            mPaintButton.style = Paint.Style.STROKE
            canvas.drawCircle(mXC, mYC, mRadius, mPaintButton)
        }
    }

    private fun drawTick(canvas: Canvas) {
        if (mChecked) {
            mPaintTick.color = mTickCheckColor
        } else {
            mPaintTick.color = mTickUncheckColor
        }
        canvas.drawPath(mPathTick, mPaintTick)
    }

    private fun drawIcon(canvas: Canvas) {
        val w = mButtonSize

        val x1 = mXC - w / 2
        val x2 = x1 + w
        val y1 = mYC - w / 2
        val y2 = y1 + w

        if (mChecked) {
            val drawable = ContextCompat.getDrawable(context, mIconChecked) as Drawable
            drawable.setBounds(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
            drawable.draw(canvas)
        } else {
            if (mIconUncheck != 0) {
                val drawable = ContextCompat.getDrawable(context, mIconUncheck) as Drawable

                drawable.setBounds(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
                drawable.draw(canvas)
            } else {
                val drawable = ContextCompat.getDrawable(context, mIconChecked) as Drawable

                drawable.colorFilter = mColorFilter
                drawable.setBounds(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
                drawable.draw(canvas)
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        val offset = (mPaintText.descent() + mPaintText.ascent()) / 2
        val y = mYC - offset
        canvas.drawText(mText!!, mXT, y, mPaintText)
    }

    private fun drawRipple(canvas: Canvas) {
        val x = mXC
        val y = mYC
        val r1 = mRippleRadius
        canvas.drawCircle(x, y, r1, mPaintRipple)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wC = (mRippleMax * 2 + mFieldText).toInt()
        val hC = mRippleMax.toInt() * 2

        setMeasuredDimension(
            resolveSize(wC, widthMeasureSpec),
            resolveSize(hC, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOffsetHor = (w - mFieldText - mButtonSize) / 2
        mOffsetVert = (h - mButtonSize) / 2

        if (mGravity == 0) {
            mXC = mRippleMax
            mXT = mRippleMax + mButtonSize / 2 + 20f
        } else {
            mXC = w - mRippleMax
            mXT = 10f
        }

        val x = mXC - mButtonSize / 2
        mYC = h / 2f

        if (mCheckStyle != 3)
            setPaths(x)
    }

    private fun setPaths(x: Float) {
        val y = mOffsetVert

        mPathRect.moveTo(x, y)
        mPathRect.rLineTo(mButtonSize, 0f)
        mPathRect.rLineTo(0f, mButtonSize)
        mPathRect.rLineTo(-mButtonSize, 0f)
        mPathRect.close()

        val w = mButtonSize
        val x1 = mXC - w * 0.28f
        val y1 = mYC
        val x2 = mXC - w * 0.1f
        val y2 = mYC + w * 0.22f
        val x3 = mXC + w * 0.28f
        val y3 = mYC - w * 0.22f

        mPathTick.moveTo(x1, y1)
        mPathTick.lineTo(x2, y2)
        mPathTick.lineTo(x3, y3)
    }

    //-----------

    // Анимация
    private fun startAnim(duration: Long) {
        val size1: Float
        val size2: Float
        if (mKeyDown) {
            size1 = 0f
            size2 = mRippleStroke
        } else {
            size1 = mAlpha.toFloat()
            size2 = 0f
        }
        val r = mRippleStart
        val anim = ValueAnimator.ofFloat(size1, size2)
        anim.duration = duration
        anim.addUpdateListener { valueAnimator ->
            val dSize = valueAnimator.animatedValue as Float
            if (mKeyDown) {
                mRippleRadius = r + dSize
            } else {
                mAlpha = dSize.toInt()
                mPaintRipple.alpha = mAlpha
            }
            invalidate()
        }
        anim.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mRippleRadius = mRippleStart
                mAlpha = RIPPLE_ALPHA
                mPaintRipple.alpha = RIPPLE_ALPHA
                mKeyDown = true
                startAnim(150)
            }

            MotionEvent.ACTION_UP -> {
                mRippleRadius = mRippleMax

                mKeyDown = false
                startAnim(300)

                mChecked = !mChecked
                invalidate()
                mListener?.invoke(this, mChecked)
            }
        }
        return true
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        this.mListener = listener
    }

    var isChecked: Boolean
        get() = mChecked
        set(value) {
            mChecked = value
            invalidate()
        }


}