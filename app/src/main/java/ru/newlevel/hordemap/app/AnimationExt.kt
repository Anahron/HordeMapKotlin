package ru.newlevel.hordemap.app

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import ru.newlevel.hordemap.R


fun View.hideToRight(valuePx: Float) {
    if (translationX != valuePx) {
        translationX = 0f
        val animator = ObjectAnimator.ofFloat(this, "translationX", valuePx)
        animator.duration = 500
        animator.start()
    }
}

fun View.showAtRight(valuePx: Float) {
    if (translationX == valuePx) {
        this.visibility = TextView.VISIBLE
        translationX = valuePx
        val animator = ObjectAnimator.ofFloat(this, "translationX", 0f)
        animator.duration = 500
        animator.start()
    }
}


fun View.showShadowAnimate() {
    ObjectAnimator.ofFloat(this, "alpha", 0f, SHADOW_QUALITY).apply {
        duration = 200
        start()
    }
}

fun View.hideShadowAnimate() {
    ObjectAnimator.ofFloat(this, "alpha", this.alpha, 0f).apply {
        duration = 200
        start()
    }
}

fun View.showInputTextAnimation() {
    if (visibility != View.VISIBLE) {
        translationY = context.convertDpToPx(55).toFloat()
        val animator = ObjectAnimator.ofFloat(this, "translationY", 0f)
        animator.duration = 300
        animator.start()
        visibility = View.VISIBLE
    }
}


fun View.animateButtonPadding() {
    if (this.paddingBottom != context.convertDpToPx(71)) {
        val paddingStart = context.convertDpToPx(16)
        val paddingEnd = context.convertDpToPx(71)
        val animator = ValueAnimator.ofInt(paddingStart, paddingEnd)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            this.setPadding(
                this.paddingLeft,
                this.paddingTop,
                this.paddingRight,
                animatedValue
            )
        }
        animator.duration = 300L
        animator.start()
    }
}

fun View.animateButtonPaddingReverse() {
    if (this.paddingBottom == context.convertDpToPx(71)) {
        val paddingStart = context.convertDpToPx(71)
        val paddingEnd = context.convertDpToPx(16)
        val animator = ValueAnimator.ofInt(paddingStart, paddingEnd)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            this.setPadding(
                this.paddingLeft,
                this.paddingTop,
                this.paddingRight,
                animatedValue
            )
        }
        animator.duration = 300L
        animator.start()
    }
}

fun View.hideInputTextAnimation() {
    if (visibility == View.VISIBLE) {
        val animator = ObjectAnimator.ofFloat(this, "translationY", context.convertDpToPx(55).toFloat())
        animator.duration = 300
        animator.start()
        animator.doOnEnd {
            this.visibility = View.GONE
        }
    }
}

fun View.loadAnimation(): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "rotationY", 0f, 360f).apply {
        duration = 2000
        repeatCount = ObjectAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        start()
    }
}


fun View.blinkAndHideShadow() {
    val colorFrom = this.context.getColor(R.color.main_green_dark)
    val colorTo = Color.TRANSPARENT
    ObjectAnimator.ofObject(
        this,
        "backgroundColor",
        ArgbEvaluator(),
        colorFrom,
        colorTo
    ).apply {
        duration = 2000
        start()
    }
}