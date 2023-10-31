package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

interface MessageStorage {
    fun sendMessage(text: String)

    fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()

    fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long)

    fun downloadFile(context: Context, uri: Uri, fileName: String?)

    fun getDownloadProgress(): MutableLiveData<Int>
}