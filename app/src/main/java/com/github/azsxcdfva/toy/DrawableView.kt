package com.github.azsxcdfva.toy

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.github.azsxcdfva.toy.utils.resolveThemedColor

class DrawableView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {
    interface Callback {
        fun onDown(x: Float, y: Float)
        fun onMove(x: Float, y: Float)
        fun onUp(x: Float, y: Float)
        fun onReset()
    }

    var callback: Callback? = null

    private val paint = Paint().apply {
        color = context.resolveThemedColor(R.attr.colorOnSurface)
        hinting = Paint.HINTING_ON
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private var path = Path()

    fun reset() {
        path = Path()

        callback?.onReset()

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)

                callback?.onDown(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)

                callback?.onMove(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                callback?.onUp(event.x, event.y)
            }
        }

        invalidate()

        return true
    }
}