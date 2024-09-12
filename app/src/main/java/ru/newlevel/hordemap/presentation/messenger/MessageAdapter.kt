package ru.newlevel.hordemap.presentation.messenger

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.JPG_EXTENSION
import ru.newlevel.hordemap.app.TAG
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

    private var messageDataModels: List<MyMessageEntity> = mutableListOf()
    private var glide: RequestManager = Glide.with(context)
    private var bottomPadding = context.convertDpToPx(80)
    private var isFirstLaunch = true

    fun setMessages(newList: MutableList<MyMessageEntity>) {
        if (isFirstLaunch) {
            val firstUnreadPosition = getFirstUnReadPosition()
            if (firstUnreadPosition != -1) {
                val separator = MyMessageEntity(0L)
                newList.add(firstUnreadPosition, separator)
            }
        }
        val diffCallback = MessageDiffCallback(messageDataModels, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageDataModels = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteSeparator() {
        Log.e(TAG, "!!!!!!!!!!!!deleteSeparator")
        val messageSeparator = messageDataModels.find { it.timestamp == 0L }
        val separatorIndex = messageDataModels.indexOf(messageSeparator)
        if (separatorIndex != -1) {
            val newList = messageDataModels.toMutableList()
            newList.removeAt(separatorIndex)
            val diffCallback = MessageDiffCallback(messageDataModels, newList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            messageDataModels = newList
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun getPosition(message: MyMessageEntity): Int {
        return messageDataModels.indexOf(message)
    }

    fun getFirstUnReadPosition(): Int {
        val firstUnRead = messageDataModels.firstOrNull { !it.isRead }
        return if (firstUnRead == null)
            -1
        else if (firstUnRead.deviceID != UserEntityProvider.userEntity.deviceID)
            messageDataModels.indexOf(firstUnRead)
        else -1


    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MessageViewHolder {
        return when (viewType) {
            ITEM_IN -> MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_in, parent, false),
                onMessageItemClickListener,
                glide,
                true, ITEM_IN
            )

            ITEM_SEPARATOR -> MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_separator, parent, false),
                onMessageItemClickListener, glide, false, ITEM_SEPARATOR
            )

            else -> MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_out, parent, false),
                onMessageItemClickListener,
                glide,
                false, ITEM_OUT
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(
        holder: MessageViewHolder, position: Int
    ) {
        if (holder.itemViewType == ITEM_SEPARATOR) {
            holder.bindSeparator()
            return
        }
        val message = messageDataModels[position]

        var someUserMessage = false
        if (position > 0) if (messageDataModels[position - 1].deviceID == message.deviceID) someUserMessage = true
        val replyMessage = if (message.replyOn > 0) messageDataModels.find {
            it.timestamp == message.replyOn
        } else null
        var x = 0f
        var y = 0f
        holder.itemView.findViewById<TableLayout>(R.id.rootLinear).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                x = event.rawX
                y = if (position + 1 == messageDataModels.size) event.rawY - bottomPadding else event.rawY - (bottomPadding / 2)
            }
            false
        }
        holder.itemView.findViewById<TableLayout>(R.id.rootLinear).setOnClickListener {
            onMessageItemClickListener.onItemClick(message, holder.itemView, x, y, holder.isInMessage)
        }
        holder.bind(message, someUserMessage, replyMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messageDataModels[position].deviceID == UserEntityProvider.userEntity.deviceID -> ITEM_OUT
            messageDataModels[position].timestamp == 0L -> {
                isFirstLaunch = false
                ITEM_SEPARATOR}
            else -> ITEM_IN
        }
    }

    override fun getItemCount(): Int {
        return messageDataModels.size
    }

    class MessageViewHolder(
        view: View,
        private val onMessageItemClickListener: OnMessageItemClickListener,
        private val glide: RequestManager,
        val isInMessage: Boolean,
        private val viewType: Int
    ) : RecyclerView.ViewHolder(view) {

        private val binding = if (viewType != ITEM_SEPARATOR) ItemMessageInBinding.bind(view) else null
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private fun setUpTimeAndTimeZone(timestamp: Long) {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            binding?.textViewTime?.text = localDateTime.format(formatter)
        }

        fun bindSeparator() {

        }

        private fun bindReply(messageDataModel: MyMessageEntity) {
            resetVisibilities()
            binding?.textViewUsername?.text = messageDataModel.userName
            NameColors.entries.find { it.id == messageDataModel.selectedMarker }?.let {
                binding?.textViewUsername?.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
            }
            if (messageDataModel.message.isNotEmpty()) {
                binding?.textViewMessage?.visibility = View.VISIBLE
                binding?.textViewMessage?.text = messageDataModel.message
            }
            if (messageDataModel.url.isNotEmpty()) setUpItemWithUrl(messageDataModel, onMessageItemClickListener, true)
            binding?.rootLinear?.setOnClickListener {
                onMessageItemClickListener.onReplyClick(messageDataModel)
            }
        }

        fun bind(messageDataModel: MyMessageEntity, isSomeUser: Boolean, replyMessageDataModel: MyMessageEntity?) {
            resetVisibilities()
            setUpTimeAndTimeZone(messageDataModel.timestamp)
            if (!messageDataModel.isRead)
                onMessageItemClickListener.isRead(messageDataModel.timestamp)
            binding?.apply {
                replyMessageDataModel?.let {
                    val inflatedView =
                        LayoutInflater.from(itemView.context).inflate(R.layout.item_message_reply, replyView, false)
                    MessageViewHolder(inflatedView, onMessageItemClickListener, glide, isInMessage, viewType).bindReply(it)
                    replyView.addView(inflatedView)
                    replyView.visibility = View.VISIBLE
                }
                textViewUsername.text = messageDataModel.userName
                NameColors.entries.find { it.id == messageDataModel.selectedMarker }?.let {
                    textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
                }
                if (isSomeUser) {
                    textViewUsername.visibility = View.GONE
                    imvProfilePhoto.visibility = View.INVISIBLE
                } else if (messageDataModel.profileImageUrl.isNotEmpty()) {
                    messageDataModel.profileImageUrl.toUri()
                    glide.load(messageDataModel.profileImageUrl.toUri())
                        .timeout(30_000)
                        .placeholder(R.drawable.img_anonymous).into(imvProfilePhoto)
                    bindImageClickListener(messageDataModel.profileImageUrl)
                } else {
                    imvProfilePhoto.setBackgroundResource(R.drawable.img_anonymous)
                }
                if (messageDataModel.message.isNotEmpty()) {
                    textViewMessage.visibility = View.VISIBLE
                    textViewMessage.text = messageDataModel.message
                }
                if (messageDataModel.url.isNotEmpty())
                    setUpItemWithUrl(messageDataModel, onMessageItemClickListener, false)
            }
        }

        private fun bindImageClickListener(url: String) {
            binding?.imvProfilePhoto?.setOnClickListener {
                onMessageItemClickListener.onImageClick(url)
            }
        }

        private fun resetVisibilities() {
            binding?.apply {
                replyView.removeAllViews()
                replyView.visibility = View.GONE
                textViewUsername.visibility = View.VISIBLE
                downloadButton.visibility = View.GONE
                imvProfilePhoto.visibility = View.VISIBLE
                imageView.visibility = View.GONE
                textViewMessage.visibility = View.GONE
                textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            }

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
            if (fileName.endsWith(JPG_EXTENSION)) {
                this?.imageView?.visibility = View.VISIBLE
                this?.imageView?.let {
                    glide.load(messageDataModel.url)
                        .placeholder(R.drawable.downloaded_image)
                        .timeout(30_000)
                        .into(it)
                }
                if (!isReply) this?.imageView?.setOnClickListener {
                    onMessageItemClickListener.onImageClick(messageDataModel.url)
                }
            } else {
                this?.downloadButton?.visibility = View.VISIBLE
                val downloadBtnText = "$fileName $fileSizeText"
                this?.downloadButton?.text = downloadBtnText
                if (!isReply) this?.downloadButton?.setOnClickListener {
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

    companion object {
        private const val ITEM_IN = 1
        private const val ITEM_OUT = 2
        private const val ITEM_SEPARATOR = 3
    }
}