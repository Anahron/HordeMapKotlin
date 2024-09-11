package ru.newlevel.hordemap.presentation.messenger

import android.view.View
import ru.newlevel.hordemap.data.db.MyMessageEntity


interface OnMessageItemClickListener {
    fun onButtonSaveClick(uri: String, fileName: String)
    fun onImageClick(url: String)
    fun onItemClick(message: MyMessageEntity, itemView: View, x: Float, y: Float, isInMessage: Boolean)
    fun onReplyClick(message: MyMessageEntity)
    fun isRead(id: Long)
}
