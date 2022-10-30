package ru.netology.animation.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.animation.R
import ru.netology.animation.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private var fillType = 0
    private var totalProgress = 0
    val MAX_PROGRESS = 1F

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            fillType = getInteger(R.styleable.StatsView_fillType, 0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        if (fillType == 1) {
            if (progress == MAX_PROGRESS && totalProgress < data.size - 1) {
                totalProgress++
                update()
            }
        }

        var startFrom = 0F
        when (fillType) {
            0, 1 -> startFrom = -90F
            2 -> startFrom = -45F
        }
        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()
            when (fillType) {
                0 -> canvas.drawArc(oval, startFrom, angle * progress, false, paint)
                1 -> {
                    if (totalProgress >= index && totalProgress < index + 1) {
                        canvas.drawArc(oval, startFrom, angle * progress, false, paint)
                    } else {
                        if (totalProgress >= index + 1)
                            canvas.drawArc(oval, startFrom, angle, false, paint)
                    }
                }
                2 -> {
                    canvas.drawArc(oval, startFrom, angle * progress / 2, false, paint)
                    canvas.drawArc(oval, startFrom, -angle * progress / 2, false, paint)
                }
            }
            Log.d("startFrom progress angle", "$startFrom $progress $angle")
            startFrom += angle


        }



        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F
        valueAnimator = ValueAnimator.ofFloat(0F, MAX_PROGRESS).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                Log.d("progress", anim.animatedValue.toString())
                invalidate()
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}