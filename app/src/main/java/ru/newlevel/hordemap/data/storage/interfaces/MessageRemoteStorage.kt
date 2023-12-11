package ru.newlevel.hordemap.data.storage.interfaces

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessageRemoteStorage {
    fun sendMessage(message: MyMessageEntity)

    fun getMessageUpdate(): Flow<List<MyMessageEntity>>

    fun stopMessageUpdate()

    fun getProfilesInMessenger(): Flow<List<UserDataModel>>

    fun deleteMessage(message: MyMessageEntity)
}