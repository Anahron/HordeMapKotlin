package ru.newlevel.hordemap.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import ru.newlevel.hordemap.R

class ScaleBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.main_green)
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = 2f * density // Толщина линии в dp
        val notchHeight = 6f * density // Высота засечек в dp
        val notchWidth = 3f * density // Ширина засечек в dp

        // Основная линия шкалы
        canvas.drawRect(0f, 0f, width, height, paint)

        // Засечки вниз
        canvas.drawRect(0f, height, notchWidth, height + notchHeight, paint)
        canvas.drawRect(width - notchWidth, height, width, height + notchHeight, paint)
    }
}

