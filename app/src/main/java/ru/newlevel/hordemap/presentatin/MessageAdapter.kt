package ru.newlevel.hordemap.presentatin

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.ItemMessageBinding
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(private val onButtonSaveClickListener: OnButtonSaveClickListener) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

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
        holder.bind(messageDataModel, onButtonSaveClickListener)
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
        fun bind(messageDataModel: MessageDataModel, onButtonSaveClickListener: OnButtonSaveClickListener) {
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
                        binding.imageView.setImageBitmap(messageDataModel.thumbnail)
                        val file = File(downloadsDir, fileName)
                        if (file.exists()) {
                            setItemsInMessage(file, messageDataModel)
                        }
                        binding.imageView.setOnClickListener { v: View? ->
                            if (messageDataModel.file != null) {
                                // openFullScreenImage(messageDataModel.file)
                            } else {
                                val storageReference =
                                    FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(strings[0])
                                //       val glideWrapper = GlideWrapper()
                                // TODO реализовать глайд      glideWrapper.load(
                                //             MapsActivity.getContext(),
                                //              storageReference,
                                //              itemImageView,
                                //              message,
                                //              fileName
                                //          )
                            }
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

        private fun setItemsInMessage(file: File, messageDataModel: MessageDataModel) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val thumbnailBitmap = ThumbnailUtils.extractThumbnail(bitmap, 120, 120)
            messageDataModel.thumbnail = thumbnailBitmap
            messageDataModel.file = file
            binding.imageView.setImageBitmap(thumbnailBitmap)
        }

        private fun openFullScreenImage(file: File) {
//            TODO реализация         val intent = Intent(requireContext, FullScreenImageActivity::class.java)
//            intent.putExtra("imageUrl", file.absolutePath)
//            MapsActivity.getContext().startActivity(intent)
        }
    }

//    class GlideWrapper {
//        fun load(
//            context: Context?,
//            storageReference: StorageReference?,
//            itemImageView: ImageView,
//            message: Message,
//            fileName: String
//        ) {
//    // TODO загрузку тоже перенести во вм  makeLongToast("Изображение загружается, подождите")
//            val requestBuilder = Glide.with(context!!).load(storageReference)
//            val options: RequestOptions =
//                RequestOptions().error(R.drawable.download_image_error).override(1024, 1024)
//                    .encodeQuality(50).diskCacheStrategy(DiskCacheStrategy.ALL)
//            requestBuilder.apply(options).into(object : CustomTarget<Drawable?>() {
//                override fun onResourceReady(
//                    resource: Drawable,
//                    transition: Transition<in Drawable>?
//                ) {
//                    val bitmap = (resource as BitmapDrawable).bitmap
//                    val thumbnailBitmap = ThumbnailUtils.extractThumbnail(bitmap, 120, 120)
//                    message.setThumbnail(thumbnailBitmap)
//                    message.setFile(saveBitmapToDownloads(bitmap, fileName))
//                    itemImageView.setImageBitmap(thumbnailBitmap)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {}
//            })
//        }
//
//        fun saveBitmapToDownloads(bitmap: Bitmap, fileName: String): File {
//            downloadsDir =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val file = File(downloadsDir, fileName)
//            try {
//                FileOutputStream(file).use { fos ->
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
//                    fos.flush()
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            MediaScannerConnection.scanFile(
//                getContext(),
//                arrayOf(downloadsDir.toString() + "/" + fileName),
//                null,
//                null
//            )
//            return file
//        }
//    }

    companion object {
        @Volatile
        private var downloadsDir: File? = null
    }
    interface OnButtonSaveClickListener {
        fun onButtonSaveClick(uri: String, fileName: String)
    }
}