package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

interface MessageRemoteStorage {
    fun sendMessage(text: String)

    fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String)

    fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()
}