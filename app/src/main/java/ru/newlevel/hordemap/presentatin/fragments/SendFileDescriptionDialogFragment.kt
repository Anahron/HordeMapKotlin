package ru.newlevel.hordemap.presentatin.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.FragmentSendFileDescriptionDialogBinding


class SendFileDescriptionDialogFragment(
    private val uri: Uri,
    private val fileName: String?,
    private val fileSize: Long,
    private val onFileDescriptionListener: OnFileDescriptionListener,
    private val isImage: Boolean
) : DialogFragment(R.layout.fragment_send_file_description_dialog) {

    private val binding: FragmentSendFileDescriptionDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        if (isImage)
            Glide.with(this@SendFileDescriptionDialogFragment)
                .load(uri)
                .into(imageViewPhoto)
        else{
            val fileSizeText = " (" + fileSize / 1000 + "kb)"
            imageViewPhoto.visibility = View.GONE
            textView3.text = "Отправить как файл: $fileName $fileSizeText"
        }
        btnSend.setOnClickListener {
            val description = editTextDescription.text.toString()
            onFileDescriptionListener.onFileDescriptionReceived(description, uri, fileName!!, fileSize)
            dismiss()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    interface OnFileDescriptionListener {
        fun onFileDescriptionReceived(description: String, photoUri: Uri, fileName: String, fileSize: Long)
    }
}
