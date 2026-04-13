package ru.newlevel.hordemap.domain.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface MessengerRepository {
    suspend fun sendMessage(message: MyMessageEntity)
    suspend fun insertLocalMessages(messages: List<MyMessageEntity>)
    suspend fun insertLocalMessage(message: MyMessageEntity)
    suspend fun updateLocalMessage(newMessage: MyMessageEntity)
    suspend fun deleteLocalMessage(message: MyMessageEntity)
    fun getLocalMessageUpdate(): Flow<List<MyMessageEntity>>
    fun getRemoteMessagesUpdate(): Flow<List<MyMessageEntity>>
    fun deleteRemoteMessage(message: MyMessageEntity)
    fun getNewMessageCount(): Flow<Int>
    suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri>
    suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean>
    fun getUsersProfilesUpdate(): Flow<List<UserDataModel>>
   suspend fun setMessageRead(id: Long)
}