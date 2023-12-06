package ru.newlevel.hordemap.presentation.messenger

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.convertDpToPx
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.ItemMessageInBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MessagesAdapter(
    private val onMessageItemClickListener: OnMessageItemClickListener, context: Context
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageDataModels: ArrayList<MyMessageEntity> = ArrayList()
    private var glide: RequestManager = Glide.with(context)
    private var bottomPadding = context.convertDpToPx(80)

    fun setMessages(newList: ArrayList<MyMessageEntity>) {
        val diffCallback = MessageDiffCallback(messageDataModels, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageDataModels = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getPosition(message: MyMessageEntity): Int {
        return messageDataModels.indexOf(message)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MessageViewHolder {
        return when (viewType) {
            ITEM_IN ->  MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_in, parent, false)
                , onMessageItemClickListener, glide, true)
            else -> MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_out, parent, false)
                , onMessageItemClickListener, glide, false)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(
        holder: MessageViewHolder, position: Int
    ) {
        val message = messageDataModels[position]
        var someUserMessage = false
        if (position > 0) if (messageDataModels[position - 1].deviceID == message.deviceID) someUserMessage = true
        val replyMessage = if (message.replyOn > 0) messageDataModels.find {
            it.timestamp == message.replyOn
        } else null
        var x = 0f
        var y = 0f
        holder.itemView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                x = event.rawX
                y = if (position+1 == messageDataModels.size) event.rawY- bottomPadding else event.rawY - (bottomPadding/2)
            }
            false
        }
        holder.itemView.setOnClickListener {
            onMessageItemClickListener.onItemClick(message, holder.itemView, x, y, holder.isInMessage)
        }
        holder.bind(message, someUserMessage, replyMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageDataModels[position].deviceID == UserEntityProvider.userEntity.deviceID) ITEM_OUT else ITEM_IN
    }

    override fun getItemCount(): Int {
        return messageDataModels.size
    }

    class MessageViewHolder(
        view: View, private val onMessageItemClickListener: OnMessageItemClickListener, private val glide: RequestManager, val isInMessage: Boolean
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemMessageInBinding.bind(view)
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private fun setUpTimeAndTimeZone(timestamp: Long) {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            binding.textViewTime.text = localDateTime.format(formatter)
        }

        private fun bindReply(messageDataModel: MyMessageEntity) {
            resetVisibilities()
            binding.textViewUsername.text = messageDataModel.userName
            NameColors.values().find { it.id == messageDataModel.selectedMarker }?.let {
                binding.textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
            }
            if (messageDataModel.message.isNotEmpty()) {
                binding.textViewMessage.visibility = View.VISIBLE
                binding.textViewMessage.text = messageDataModel.message
            }
            if (messageDataModel.url.isNotEmpty()) setUpItemWithUrl(messageDataModel, onMessageItemClickListener, true)
            binding.rootLinear.setOnClickListener {
                onMessageItemClickListener.onReplyClick(messageDataModel)
            }
        }

        fun bind(messageDataModel: MyMessageEntity, isSomeUser: Boolean, replyMessageDataModel: MyMessageEntity?) {
            resetVisibilities()
            setUpTimeAndTimeZone(messageDataModel.timestamp)
            binding.apply {
                replyMessageDataModel?.let {
                    val inflatedView =
                        LayoutInflater.from(itemView.context).inflate(R.layout.item_message_reply, replyView, false)
                    MessageViewHolder(inflatedView, onMessageItemClickListener, glide, isInMessage).bindReply(it)
                    replyView.addView(inflatedView)
                    replyView.visibility = View.VISIBLE
                }
                textViewUsername.text = messageDataModel.userName
                NameColors.values().find { it.id == messageDataModel.selectedMarker }?.let {
                    textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
                }
                if (isSomeUser) {
                    textViewUsername.visibility = View.GONE
                    imvProfilePhoto.visibility = View.INVISIBLE
                } else if (messageDataModel.profileImageUrl.isNotEmpty())
                    glide.load(messageDataModel.profileImageUrl.toUri())
                        .thumbnail(1f)
                        .timeout(30_000)
                        .placeholder(R.drawable.img_anonymous).into(imvProfilePhoto)
                else binding.imvProfilePhoto.setImageResource(R.drawable.img_anonymous)
                if (messageDataModel.message.isNotEmpty()) {
                    textViewMessage.visibility = View.VISIBLE
                    textViewMessage.text = messageDataModel.message
                }
                if (messageDataModel.url.isNotEmpty())
                    setUpItemWithUrl(messageDataModel, onMessageItemClickListener, false)
            }
        }

        private fun resetVisibilities() = with(binding) {
            replyView.removeAllViews()
            replyView.visibility = View.GONE
            textViewUsername.visibility = View.VISIBLE
            downloadButton.visibility = View.GONE
            imvProfilePhoto.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            textViewMessage.visibility = View.GONE
            textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
        }

        private fun setUpItemWithUrl(
            messageDataModel: MyMessageEntity, onMessageItemClickListener: OnMessageItemClickListener, isReply: Boolean
        ) = with(binding) {
            val fileName = messageDataModel.fileName
            val fileSize = messageDataModel.fileSize
            val fileSizeText = if ((fileSize / 1000) < 1000) " (" + (fileSize / 1000) + "kb)"
            else " (" + String.format(
                "%.1f", (fileSize.toDouble() / 1000000)
            ) + "Mb)"
            if (fileName.endsWith(".jpg")) {
                imageView.visibility = View.VISIBLE
                glide.load(messageDataModel.url).thumbnail(0.1f).placeholder(R.drawable.downloaded_image)
                    .timeout(30_000)
                    .into(imageView)
                if (!isReply) imageView.setOnClickListener {
                    onMessageItemClickListener.onImageClick(messageDataModel.url)
                }

            } else {
                downloadButton.visibility = View.VISIBLE
                val downloadBtnText = "$fileName $fileSizeText"
                downloadButton.text = downloadBtnText
                if (!isReply) downloadButton.setOnClickListener {
                    onMessageItemClickListener.onButtonSaveClick(
                        messageDataModel.url, fileName
                    )
                }
            }
        }
    }

    class MessageDiffCallback(
        private val oldList: List<MyMessageEntity>, private val newList: List<MyMessageEntity>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldMessage = oldList[oldItemPosition]
            val newMessage = newList[newItemPosition]
            return oldMessage.message == newMessage.message && oldMessage.profileImageUrl == newMessage.profileImageUrl && oldMessage.selectedMarker == newMessage.selectedMarker && oldMessage.userName == newMessage.userName
        }
    }

    interface OnMessageItemClickListener {
        fun onButtonSaveClick(uri: String, fileName: String)
        fun onImageClick(url: String)
        fun onItemClick(message: MyMessageEntity, itemView: View, x: Float, y: Float, isInMessage: Boolean)
        fun onReplyClick(message: MyMessageEntity)
    }

    companion object {
        private const val ITEM_IN = 1
        private const val ITEM_OUT = 2
    }
}