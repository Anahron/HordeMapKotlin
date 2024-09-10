package ru.newlevel.hordemap.app

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable

class AbcRoundedShadowShapeDrawable(
    private val cornerRadius: Float,
    private val shadowRadius: Float,
    private val shadowColor: Int,
    private val backgroundColor: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
        setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
    }

    private val shadowBounds = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val halfShadow = shadowRadius / 2
        shadowBounds.set(
            bounds.left + halfShadow,
            bounds.top + halfShadow,
            bounds.right - halfShadow,
            bounds.bottom - halfShadow
        )
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.drawRoundRect(shadowBounds, cornerRadius, cornerRadius, paint)
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}