package ru.newlevel.hordemap.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import ru.newlevel.hordemap.R


class CustomMapDialogDrawable (private val context: Context, private val isTransparent: Boolean = true) : Drawable() {

    private val paint = Paint().apply {
        color = if (isTransparent) context.getColor(R.color.white) else context.getColor(R.color.white_midle)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = context.getColor(R.color.main_green_dark) // Цвет рамки
        strokeWidth = dpToPx(2f) // Толщина рамки 2dp
        style = Paint.Style.STROKE
    }

    private val path = Path()


    override fun draw(canvas: Canvas) {
        // Обновляем путь
        updatePath()

        // Закрашиваем
        canvas.clipPath(path)
        canvas.drawRect(bounds, paint)

        // Рисуем рамку
        canvas.drawPath(path, borderPaint)

    }

    private fun updatePath() {
        // Очищаем путь перед обновлением
        path.reset()

        // Путь для рамки с угловыми скосами
        path.lineTo(bounds.right.toFloat(), bounds.top.toFloat() )
        path.lineTo(bounds.right.toFloat(), bounds.bottom.toFloat() - dpToPx(14f))
        path.lineTo(bounds.right.toFloat() - dpToPx(12f), bounds.bottom.toFloat())
        path.lineTo(bounds.left.toFloat(), bounds.bottom.toFloat())
        path.lineTo(bounds.left.toFloat(), bounds.top.toFloat())
        path.close()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
