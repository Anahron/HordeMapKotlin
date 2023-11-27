package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessageRemoteStorage {
    fun sendMessage(text: String)

    fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String)

    fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()

    fun getProfilesInMessenger(): MutableLiveData<List<UserDataModel>>
    suspend fun sendUserData(userData: UserDataModel): Boolean
}