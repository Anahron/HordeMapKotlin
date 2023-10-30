package ru.newlevel.hordemap.presentatin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.ItemMessageBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MessagesAdapter(
    private val onButtonSaveClickListener: OnButtonSaveClickListener,
    private val onImageClickListener: OnImageClickListener
) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageDataModels: ArrayList<MessageDataModel>? = null

    fun setMessages(newMessageDataModels: ArrayList<MessageDataModel>) {
        messageDataModels = newMessageDataModels
        notifyDataSetChanged()
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
        val messageDataModel: MessageDataModel? = messageDataModels?.get(position)
        holder.bind(messageDataModel, onButtonSaveClickListener, onImageClickListener)
    }

    override fun getItemCount(): Int {
        return messageDataModels?.size ?: 0
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemMessageBinding by viewBinding()

        private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        private val timeZone = TimeZone.getDefault()

        init {
            dateFormat.timeZone = timeZone
        }


        fun bind(
            messageDataModel: MessageDataModel?,
            onButtonSaveClickListener: OnButtonSaveClickListener,
            onImageClickListener: OnImageClickListener
        ) {
            binding.downloadButton.visibility = View.GONE
            binding.imageView.visibility = View.GONE
            if (messageDataModel != null) {
                binding.textViewUsername.text = messageDataModel.userName
                binding.textViewTime.text = dateFormat.format(Date(messageDataModel.timestamp))
                val messageText: String = messageDataModel.message

                if (messageText.startsWith("https://firebasestorage")) {
                    try {
                        val fileName = messageDataModel.fileName
                        val fileSizeText = if (messageDataModel.fileSize > 0)
                            " (" + messageDataModel.fileSize / 1000 + "kb)" else ""
                        binding.textViewMessage.text = getContentText(fileName, fileSizeText)
                        if (fileName.contains(".jpg")) {
                            binding.downloadButton.visibility = View.GONE
                            binding.imageView.visibility = View.VISIBLE
                            Glide.with(itemView.context)
                                .load(messageDataModel.message)
                                .thumbnail(0.1f)
                                .timeout(30_000)
                                .into(binding.imageView)
                            binding.imageView.setOnClickListener {
                                onImageClickListener.onImageClick(messageDataModel.message)
                            }

                        } else {
                            binding.downloadButton.visibility = View.VISIBLE
                            binding.downloadButton.setOnClickListener {
                                onButtonSaveClickListener.onButtonSaveClick(messageDataModel.message, fileName)
                            }
                        }
                    } catch (e: Exception) {
                        binding.textViewMessage.text = messageText
                        e.printStackTrace()
                    }
                } else {
                    binding.textViewMessage.text = messageText
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

    interface OnButtonSaveClickListener {
        fun onButtonSaveClick(uri: String, fileName: String)
    }

    interface OnImageClickListener {
        fun onImageClick(url: String)
    }
}