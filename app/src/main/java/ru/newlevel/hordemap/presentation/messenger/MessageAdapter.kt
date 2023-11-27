package ru.newlevel.hordemap.presentation.messenger

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.ItemMessageInBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val onButtonSaveClickListener: OnButtonSaveClickListener, private val onImageClickListener: OnImageClickListener
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageDataModels: ArrayList<MessageDataModel> = ArrayList()
    private var profiles: ArrayList<UserDomainModel> = ArrayList()

    fun setMessages(newList: ArrayList<MessageDataModel>) {
        val diffCallback = MessageDiffCallback(messageDataModels, newList, profiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageDataModels = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun setNewProfilesList(profiles: List<UserDomainModel>?) {
        this.profiles = profiles as ArrayList<UserDomainModel>
        setMessages(messageDataModels)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MessageViewHolder {
        val view = when (viewType) {
            ITEM_IN -> LayoutInflater.from(parent.context).inflate(R.layout.item_message_in, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.item_message_out, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder, position: Int
    ) {
        val message = messageDataModels[position]
        var someUserMessage = false
        if (position > 1) if (messageDataModels[position - 1].deviceID == message.deviceID) someUserMessage = true

        CoroutineScope(Dispatchers.Main).launch {
            val deferredUserData = async(Dispatchers.IO) {
                profiles.find {
                    it.deviceID == message.deviceID
                }
            }
            val userData = deferredUserData.await()
            userData?.let {
                message.userProfilePhoto = it.profileImageUrl
                message.userName = it.name
                message.selectedMarker = it.selectedMarker
            }
            holder.bind(message, onButtonSaveClickListener, onImageClickListener, someUserMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageDataModels[position].deviceID == UserEntityProvider.userEntity.deviceID) ITEM_OUT else ITEM_IN
    }

    override fun getItemCount(): Int {
        return messageDataModels.size
    }

    class MessageViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemMessageInBinding.bind(view)

        @SuppressLint("SimpleDateFormat")
        private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        private val timeZone = TimeZone.getDefault()
        private val glide by lazy {
            Glide.with(itemView.context) }

        init {
            dateFormat.timeZone = timeZone
        }

        private fun setUpTimeAndTimeZone(timestamp: Long) {
            binding.apply {
                textViewTime.text = dateFormat.format(Date(timestamp))
            }
        }

        fun bind(
            messageDataModel: MessageDataModel,
            onButtonSaveClickListener: OnButtonSaveClickListener,
            onImageClickListener: OnImageClickListener,
            isSomeUser: Boolean
        ) {
            binding.apply {
                val message = messageDataModel.message
                val url = messageDataModel.url
                resetVisibilities()
                setUpTimeAndTimeZone(messageDataModel.timestamp)
                textViewUsername.text = messageDataModel.userName
                NameColors.values().find { it.id == messageDataModel.selectedMarker }?.let {
                    textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
                }
                if (isSomeUser) {
                    textViewUsername.visibility = View.GONE
                    imvProfilePhoto.visibility = View.INVISIBLE
                } else if (messageDataModel.userProfilePhoto.isNotEmpty()) {
                    glide.load(messageDataModel.userProfilePhoto.toUri()).thumbnail(1f)
                        .timeout(30_000)
                        .placeholder(R.drawable.img_anonymous).into(imvProfilePhoto)
                }
                else {
                    imvProfilePhoto.setImageResource(R.drawable.img_anonymous)
                }
                if (message.isNotEmpty()) {
                    textViewMessage.visibility = View.VISIBLE
                    textViewMessage.text = message
                }
                if (url.isNotEmpty())
                    setUpItemWithUrl(messageDataModel, onButtonSaveClickListener, onImageClickListener)
                rootLinear.requestLayout()
            }
        }

        private fun resetVisibilities() = with(binding) {
            textViewUsername.visibility = View.VISIBLE
            downloadButton.visibility = View.GONE
            imvProfilePhoto.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            textViewMessage.visibility = View.GONE
            textViewUsername.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
        }

        private fun setUpItemWithUrl(
            messageDataModel: MessageDataModel, onButtonSaveClickListener: OnButtonSaveClickListener,
            onImageClickListener: OnImageClickListener,
        ) = with(binding) {
            val fileName = messageDataModel.fileName
            val fileSize = messageDataModel.fileSize
            val fileSizeText = if ((fileSize / 1000) < 1000) " (" + (fileSize / 1000) + "kb)"
            else " (" + String.format(
                "%.1f", (fileSize.toDouble() / 1000000)
            ) + "Mb)"
            if (fileName.contains(".jpg")) {
                imageView.visibility = View.VISIBLE
                glide.load(messageDataModel.url).thumbnail(0.1f).timeout(30_000)
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
    }

    enum class NameColors(val resourceId: Int, val id: Int) {
        RED(R.color.red, 0),
        YELLOW(R.color.yellow, 1),
        GREEN(R.color.green, 2),
        BLUE(R.color.light_blue_900, 3),
        PURPLE(R.color.purple, 4)
    }

    class MessageDiffCallback(
        private val oldList: List<MessageDataModel>,
        private val newList: List<MessageDataModel>,
        private val profiles: List<UserDomainModel>?
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldMessage = oldList[oldItemPosition]
            val newMessage = newList[newItemPosition]
            val userProfile = getUserProfile(newMessage.deviceID)

            return oldMessage.message == newMessage.message
                    && oldMessage.userProfilePhoto == userProfile?.profileImageUrl
                    && oldMessage.selectedMarker == userProfile.selectedMarker
                    && oldMessage.userName == userProfile.name
        }
        private fun getUserProfile(deviceID: String): UserDomainModel? {
            return profiles?.find { it.deviceID == deviceID }
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