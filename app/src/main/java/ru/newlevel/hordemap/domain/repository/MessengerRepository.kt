package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

interface MessengerRepository {
    fun sendMessage(text: String)

    fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String)

    fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()

    suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): String

    fun downloadFile(context: Context, uri: Uri, fileName: String?)

    fun getDownloadProgress(): MutableLiveData<Int>
}