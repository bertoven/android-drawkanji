package com.example.drawkanji.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.drawkanji.R

class DrawView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    interface OnDrawLineCompletedListener {
        fun onDrawLineCompleted(bitmap: Bitmap)
    }

    private var onDrawLineCompletedListener: OnDrawLineCompletedListener? = null
    private var previousBackgroundDrawable: Drawable? = null

    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        strokeWidth = 32f
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val xPos = event.x
        val yPos = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(xPos, yPos)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(xPos, yPos)
            }
            MotionEvent.ACTION_UP -> {
                onDrawLineCompletedListener?.onDrawLineCompleted(getBitmapFromView())
                path.reset()
            }
            else -> {
                return false
            }
        }
        invalidate()
        return true
    }

    override fun setBackgroundColor(color: Int) {
        previousBackgroundDrawable = this.background
        super.setBackgroundColor(color)
    }

    override fun setBackground(background: Drawable?) {
        previousBackgroundDrawable = this.background
        super.setBackground(background)
    }

    private fun getBitmapFromView(): Bitmap {
        // Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        // Get the view's background
        val bgDrawable = background
        if (bgDrawable != null) {
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.BLACK)
        }
        setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        // draw the view on the canvas
        draw(canvas)
        // return the bitmap
        return returnedBitmap
    }

    fun setPreviousBackground() {
        background = previousBackgroundDrawable
    }

    fun setOnDrawLineCompletedListener(listener: OnDrawLineCompletedListener?) {
        onDrawLineCompletedListener = listener
    }
}