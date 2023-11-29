package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessageRemoteStorage {
    fun sendMessage(message: MessageDataModel)

    fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>>

    fun stopMessageUpdate()

    fun getProfilesInMessenger(): MutableLiveData<List<UserDataModel>>
}