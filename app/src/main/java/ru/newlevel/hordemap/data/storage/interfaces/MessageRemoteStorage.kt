package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessageRemoteStorage {
    fun sendMessage(message: MyMessageEntity)

    fun getMessageUpdate(): MutableLiveData<List<MyMessageEntity>>

    fun stopMessageUpdate()

    fun getProfilesInMessenger(): MutableLiveData<List<UserDataModel>>
    fun deleteMessage(message: MyMessageEntity)
}