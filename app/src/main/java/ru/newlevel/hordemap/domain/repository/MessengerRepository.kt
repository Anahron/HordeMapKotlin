package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessengerRepository {
    fun sendMessage(text: String)

    fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String)

    fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()

    suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): String

    fun downloadFile(context: Context, uri: Uri, fileName: String?)

    fun getDownloadProgress(): MutableLiveData<Int>

    fun getUsersProfiles(): MutableLiveData<List<UserDataModel>>
}