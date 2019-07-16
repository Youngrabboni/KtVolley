package com.diamondedge.ktvolley.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView

class BouncingBall(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    private var ballX = 0f
    private var ballY = 0f
    private var speedX = 20f
    private var speedY = 10f
    private val h: Handler = Handler()
    private val ballWidth: Float
    private val paint = Paint()
    private val ball: ShapeDrawable

    private val r = Runnable { invalidate() }

    init {
        paint.style = Paint.Style.FILL
        ballWidth = convertDpToPixel(context, 70f)
        val circle = OvalShape()
        circle.resize(ballWidth, ballWidth)
        ball = ShapeDrawable(circle)
        changeColor()
    }

    private fun convertDpToPixel(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    private fun changeColor() {
        val red = randomColor()
        val green = randomColor()
        val blue = randomColor()
        val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
        val dim = 2
        val darkColor = -0x1000000 or (red / dim shl 16) or (green / dim shl 8) or blue / dim
        ball.paint.shader = RadialGradient(ballWidth/3, ballWidth/4, ballWidth/2, color, darkColor, Shader.TileMode.CLAMP)
    }

    private fun randomColor() = (127 + Math.random() * 127).toInt()

    override fun onDraw(canvas: Canvas) {
        ballX += speedX
        ballY += speedY
        if (ballX > width - ballWidth || ballX < 0) {
            speedX *= -1
            changeColor()
        }
        if (ballY > height - ballWidth || ballY < 0) {
            speedY *= -1
            changeColor()
        }
        canvas.save()
        canvas.translate(ballX, ballY)
        ball.draw(canvas)
        canvas.restore()
        h.postDelayed(r, 10L)
    }
}