package com.reminderwater.Dashbord

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.PI
import kotlin.math.sin

class WaterGlassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val glassPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val waterPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private var waterLevel: Float = 0f
    private var wavePhase = 0f
    private var animator: ValueAnimator? = null
    private var waveAnimator: ValueAnimator? = null
    private var waveEffectEnabled = true
    private var waveHeight = 10f
    private var waveSpeed = 2000L

    fun setWaveEffectEnabled(enabled: Boolean) {
        waveEffectEnabled = enabled
        if (enabled) {
            startWaveAnimation()
        } else {
            waveAnimator?.cancel()
        }
    }

    fun setWaveHeight(height: Float) {
        waveHeight = height
        invalidate()
    }

    fun setWaveSpeed(speed: Long) {
        waveSpeed = speed
        waveAnimator?.duration = speed
    }

    fun getWaterLevel(): Float = waterLevel

    fun setWaterLevel(level: Float, animate: Boolean = true) {
        val targetLevel = level.coerceIn(0f, 1f)
        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(waterLevel, targetLevel).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    waterLevel = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            waterLevel = targetLevel
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (waveEffectEnabled) {
            startWaveAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }

    private fun startWaveAnimation() {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, 2 * PI.toFloat()).apply {
            duration = waveSpeed
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                wavePhase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimations() {
        animator?.cancel()
        waveAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val margin = minOf(width, height) * 0.1f
        textPaint.textSize = minOf(width, height) * 0.08f

        // Draw glass
        val glassWidth = width - (margin * 2)
        val glassHeight = height - (margin * 2) - (textPaint.textSize * 2)
        val glassLeft = (width - glassWidth) / 2
        val glassTop = margin + textPaint.textSize
        val glassRect = RectF(glassLeft, glassTop, glassLeft + glassWidth, glassTop + glassHeight)
        val cornerRadius = minOf(glassWidth, glassHeight) * 0.05f

        canvas.drawRoundRect(glassRect, cornerRadius, cornerRadius, glassPaint)

        // Draw water with wave effect
        if (waterLevel > 0) {
            val waterHeight = glassRect.height() * waterLevel
            val waterTop = glassRect.bottom - waterHeight

            val wavePath = Path().apply {
                moveTo(glassRect.left, waterTop)

                val waveWidth = glassRect.width()
                val currentWaveHeight = waveHeight * (waterLevel + 0.2f)
                val xStep = waveWidth / 20

                for (x in glassRect.left.toInt() until glassRect.right.toInt() + xStep.toInt() step xStep.toInt()) {
                    val xF = x.toFloat()
                    val angle = ((xF - glassRect.left) / waveWidth * 2 * PI + wavePhase).toFloat()
                    val y = waterTop + sin(angle) * currentWaveHeight
                    lineTo(xF, y)
                }

                lineTo(glassRect.right, glassRect.bottom)
                lineTo(glassRect.left, glassRect.bottom)
                close()
            }

            canvas.drawPath(wavePath, waterPaint)
        }

        // Draw percentage text
        val text = "${(waterLevel * 100).toInt()}%"
        canvas.drawText(text, width / 2, glassTop - textPaint.textSize / 2, textPaint)
    }
}