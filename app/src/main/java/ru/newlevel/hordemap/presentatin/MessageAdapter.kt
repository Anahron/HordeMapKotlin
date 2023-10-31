package ru.newlevel.hordemap.presentatin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.ItemMessageBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        val messageDataModel: MessageDataModel = messageDataModels[position]
        holder.bind(messageDataModel, onButtonSaveClickListener, onImageClickListener)
    }

    override fun getItemCount(): Int {
        return messageDataModels.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemMessageBinding.bind(itemView)
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
            downloadButton.visibility = View.GONE
            downloadButton.visibility = View.GONE
            imageView.visibility = View.GONE
            textViewUsername.text = messageDataModel.userName
            textViewTime.text = dateFormat.format(Date(messageDataModel.timestamp))
            val messageText = messageDataModel.message
            if (messageText.isEmpty())
                textViewMessage.visibility = View.GONE
            else
                textViewMessage.text = messageText
            if (messageDataModel.url.isNotEmpty()) {
                try {
                    val fileName = messageDataModel.fileName

                    val fileSizeText = if ((messageDataModel.fileSize / 1000) < 1000)
                        " (" + (messageDataModel.fileSize / 1000) + "kb)"
                    else
                        " (" + String.format("%.1f", (messageDataModel.fileSize.toDouble() / 1000000)) + "Mb)"
                    if (fileName.contains(".jpg")) {
                        downloadButton.visibility = View.GONE
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
                        textViewMessage.text = messageText
                        downloadButton.text = getContentText(fileName, fileSizeText)
                        downloadButton.setOnClickListener {
                            onButtonSaveClickListener.onButtonSaveClick(
                                messageDataModel.url,
                                fileName
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun getContentText(fileName: String, fileSizeText: String): String {
            return if (fileName.endsWith(".jpg")) {
                "Image:$fileSizeText"
            } else {
                fileName + fileSizeText
            }
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
}