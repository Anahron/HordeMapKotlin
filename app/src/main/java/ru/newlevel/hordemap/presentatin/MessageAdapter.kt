package ru.newlevel.hordemap.presentatin

import android.annotation.SuppressLint
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


class MessagesAdapter(private val onButtonSaveClickListener: OnButtonSaveClickListener, private val onImageClickListener: OnImageClickListener) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageDataModels: ArrayList<MessageDataModel>? = null

    fun setMessages(newMessageDataModels: ArrayList<MessageDataModel>) {
        println("Пришли в setMessages с количеством сообщений " + newMessageDataModels.size)
        messageDataModels = newMessageDataModels
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MessageDataModel? {
        return if (messageDataModels != null && position >= 0 && position < messageDataModels!!.size) {
            messageDataModels!![position]
        } else null
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
        val messageDataModel: MessageDataModel = messageDataModels!![position]
        holder.bind(messageDataModel, onButtonSaveClickListener, onImageClickListener)
    }

    override fun getItemCount(): Int {
        return if (messageDataModels != null) messageDataModels!!.size else 0
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemMessageBinding by viewBinding()

        @SuppressLint("SimpleDateFormat")
        private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        private val timeZone = TimeZone.getDefault()

        @SuppressLint("SetTextI18n")
        fun bind(
            messageDataModel: MessageDataModel,
            onButtonSaveClickListener: OnButtonSaveClickListener,
            onImageClickListener: OnImageClickListener
        ) {
            dateFormat.timeZone = timeZone
            binding.textViewUsername.text = messageDataModel.userName
            binding.textViewTime.text = dateFormat.format(Date(messageDataModel.timestamp))
            val messageText: String = messageDataModel.message
            if (messageText.startsWith("https://firebasestorage")) {
                try {
                    val strings = messageText.split("&&&").toTypedArray()
                    val hasFileSize = strings.size == 3
                    val fileName = strings[1]
                    val fileSizeText =
                        if (hasFileSize) " (" + strings[2].toInt() / 1000 + "kb)" else ""
                    binding.textViewMessage.text = getContentText(fileName, fileSizeText)
                    binding.imageView.visibility = View.GONE
                    binding.downloadButton.visibility = View.GONE
                    if (fileName.endsWith(".jpg")) {
                        binding.imageView.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(strings[0])
                            .into(binding.imageView)

                        binding.imageView.setOnClickListener {
                            onImageClickListener.onImageClick(strings[0])
                        }
                    } else {
                        binding.downloadButton.visibility = View.VISIBLE
                        binding.downloadButton.setOnClickListener {
                            onButtonSaveClickListener.onButtonSaveClick(strings[0], fileName)
                        }
                    }
                } catch (e: Exception) {
                    binding.textViewMessage.text = messageText
                    e.printStackTrace()
                }
            } else {
                binding.downloadButton.visibility = View.GONE
                binding.imageView.visibility = View.GONE
                binding.textViewMessage.text = messageText
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