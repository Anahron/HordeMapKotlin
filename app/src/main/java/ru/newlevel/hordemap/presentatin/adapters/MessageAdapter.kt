package ru.newlevel.hordemap.presentatin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.ItemMessageInBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val onButtonSaveClickListener: OnButtonSaveClickListener,
    private val onImageClickListener: OnImageClickListener
) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageDataModels: ArrayList<MessageDataModel> = ArrayList()

    fun setMessages(newList: ArrayList<MessageDataModel>) {
        val diffCallback = MessageDiffCallback(messageDataModels, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageDataModels = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val view = when (viewType) {
            ITEM_IN -> LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_in, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_out, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        holder.bind(messageDataModels[position], onButtonSaveClickListener, onImageClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageDataModels[position].deviceID == UserEntityProvider.userEntity?.deviceID) ITEM_OUT else ITEM_IN
    }

    override fun getItemCount(): Int {
        return messageDataModels.size
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemMessageInBinding.bind(view)
        @SuppressLint("SimpleDateFormat")
        private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        private val timeZone = TimeZone.getDefault()

        init {
            dateFormat.timeZone = timeZone
        }

        fun bind(
            messageDataModel: MessageDataModel,
            onButtonSaveClickListener: OnButtonSaveClickListener,
            onImageClickListener: OnImageClickListener
        ) = with(binding) {
            val message = messageDataModel.message
            val fileName = messageDataModel.fileName
            val fileSize = messageDataModel.fileSize
            val url = messageDataModel.url
            downloadButton.visibility = View.GONE
            imageView.visibility = View.GONE
            textViewMessage.visibility = View.GONE
            textViewUsername.text = messageDataModel.userName
            textViewTime.text = dateFormat.format(Date(messageDataModel.timestamp))
            if (message.isNotEmpty()) {
                textViewMessage.visibility = View.VISIBLE
                textViewMessage.text = message
            }
            if (url.isNotEmpty()) {
                val fileSizeText = if ((fileSize / 1000) < 1000)
                    " (" + (fileSize / 1000) + "kb)"
                else
                    " (" + String.format(
                        "%.1f",
                        (fileSize.toDouble() / 1000000)
                    ) + "Mb)"
                if (fileName.contains(".jpg")) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(messageDataModel.url)
                        .thumbnail(0.1f)
                        .timeout(30_000)
                        .into(imageView)
                    imageView.setOnClickListener {
                        onImageClickListener.onImageClick(messageDataModel.url)
                    }

                } else {
                    downloadButton.visibility = View.VISIBLE
                    val downloadBtnText = "$fileName $fileSizeText"
                    downloadButton.text = downloadBtnText
                    downloadButton.setOnClickListener {
                        onButtonSaveClickListener.onButtonSaveClick(
                            messageDataModel.url, fileName
                        )
                    }
                }
            }
            rootLinear.requestLayout()
        }
    }

    class MessageDiffCallback(
        private val oldList: List<MessageDataModel>,
        private val newList: List<MessageDataModel>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].message == newList[newItemPosition].message
        }
    }

    interface OnButtonSaveClickListener {
        fun onButtonSaveClick(uri: String, fileName: String)
    }

    interface OnImageClickListener {
        fun onImageClick(url: String)
    }

    companion object {
        private const val ITEM_IN = 1
        private const val ITEM_OUT = 2
    }
}