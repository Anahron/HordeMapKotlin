package ru.newlevel.hordemap.presentation.messenger

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.hideShadowAnimate
import ru.newlevel.hordemap.app.showShadowAnimate
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.databinding.FragmentMessengerBinding

class MessagePopupMenu(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val binding: FragmentMessengerBinding,
    private val onDeleteClicked: (MyMessageEntity) -> Unit,
    private val onEditClicked: (MyMessageEntity) -> Unit,
    private val onReplyClicked: (MyMessageEntity) -> Unit,
    private val onCopyClicked: (MyMessageEntity) -> Unit,
    private val onDismiss: (Boolean) -> Unit
) {

    fun showPopupMenu(itemView: View, message: MyMessageEntity, layoutRes: Int, x: Float, y: Float) {
        binding.shadow.showShadowAnimate()
        val popupMenu = PopupWindow(context).apply {
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            contentView = layoutInflater.inflate(layoutRes, itemView.rootView as ViewGroup, false)
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.round_white))
            elevation = 18f
            isFocusable = true
            isOutsideTouchable = true
        }

        popupMenu.contentView?.findViewById<MaterialButton>(R.id.btnDeleteMessage)?.setOnClickListener {
            popupMenu.dismiss()
            CoroutineScope(Dispatchers.IO).launch {
                onDeleteClicked(message)
            }
        }

        popupMenu.contentView?.findViewById<MaterialButton>(R.id.btnEditMessage)?.setOnClickListener {
            popupMenu.dismiss()
            onEditClicked(message)
        }

        popupMenu.contentView?.findViewById<MaterialButton>(R.id.btnReplyMessage)?.setOnClickListener {
            popupMenu.dismiss()
            onReplyClicked(message)
        }

        popupMenu.contentView?.findViewById<MaterialButton>(R.id.btnCopyMessage)?.setOnClickListener {
            popupMenu.dismiss()
            onCopyClicked(message)
        }

        popupMenu.showAtLocation(itemView, Gravity.NO_GRAVITY, x.toInt(), y.toInt())

        popupMenu.setOnDismissListener {
            binding.shadow.hideShadowAnimate()
            onDismiss(false)
        }
    }
}
