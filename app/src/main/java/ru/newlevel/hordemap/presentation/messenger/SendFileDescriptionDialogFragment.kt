package ru.newlevel.hordemap.presentation.messenger

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
    private var fileName: String,
    private val fileSize: Long,
    private val onFileDescriptionListener: OnFileDescriptionListener,
    private val isImage: Boolean
) : DialogFragment(R.layout.fragment_send_file_description_dialog) {

    private val binding: FragmentSendFileDescriptionDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (isImage)
            Glide.with(this@SendFileDescriptionDialogFragment)
                .load(uri)
                .into(imageViewPhoto)
        else{
            val fileSizeText = " (" + fileSize / 1000 + "kb)"
            imageViewPhoto.visibility = View.GONE
            textView3.text = resources.getText(R.string.send_as_file)
            fileDiscriptions.visibility = View.VISIBLE
            val descriptionText =  "$fileName $fileSizeText"
            fileDiscriptions.text = descriptionText
        }
        btnSend.setOnClickListener {
            val description = editTextDescription.text.toString()
            onFileDescriptionListener.onFileDescriptionReceived(description, uri, fileName, fileSize)
            dismiss()
        }
        btnCancel.setOnClickListener {
            onFileDescriptionListener.onFileDescriptionDialogDismiss()
            dismiss()
        }
    }

    interface OnFileDescriptionListener {
        fun onFileDescriptionReceived(description: String, photoUri: Uri, fileName: String, fileSize: Long)

        fun onFileDescriptionDialogDismiss()
    }
}
