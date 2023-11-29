package ru.newlevel.hordemap.app

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


class ResizeAnimation(view: View, targetWidth: Int) : Animation() {
    private val startHeight: Int
    private val targetHeight: Int
    private var view: View

    init {
        this.view = view
        this.targetHeight = targetWidth
        startHeight = view.height
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val newHeight = (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        view.layoutParams.height = newHeight
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}