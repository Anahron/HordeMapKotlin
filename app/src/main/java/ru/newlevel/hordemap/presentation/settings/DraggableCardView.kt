package ru.newlevel.hordemap.presentation.settings

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.cardview.widget.CardView
import ru.newlevel.hordemap.R
import kotlin.math.abs

class DraggableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr), View.OnTouchListener {

    interface OnCardDragListener {
        fun onCardSwiped(next: Boolean)
    }

    private var startX = 0f
    private var startY = 0f
    private var offsetX = 0f
    private var isDragging = false
    private var newX = 0f

    init {
        setOnTouchListener(this)
    }

    private var onCardDragListener: OnCardDragListener? = null

    fun setOnCardDragListener(listener: OnCardDragListener) {
        this.onCardDragListener = listener
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val threshold = context.resources.displayMetrics.widthPixels / 8
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                newX = 0f
                startX = event.rawX
                startY = event.rawY
                offsetX = view.x - startX
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    newX = event.rawX - startX
                    if (view.id == R.id.cardViewSettings && newX < 0)
                        view.translationX = newX
                    else if (view.id != R.id.cardViewSettings && newX > 0)
                        view.translationX = newX
                    if (newX < 0 && abs(newX) > threshold) { //свайп влево
                        isDragging = false
                        onCardDragListener?.onCardSwiped(true)
                        view.animate().translationX(0f).apply {
                            startDelay = 400
                            duration = 100
                        }
                    } else if (newX > 0 && abs(newX) > threshold) { //свайп вправо
                        isDragging = false
                        onCardDragListener?.onCardSwiped(false)
                        view.animate().translationX(0f).apply {
                            startDelay = 400
                            duration = 100
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    view.animate().translationX(0f).apply {
                        startDelay = 0
                        duration = 50
                    }
                }
            }
        }
        return true // Consume the touch event
    }
}
