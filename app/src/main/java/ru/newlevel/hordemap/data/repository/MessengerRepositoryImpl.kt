package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.db.MessageDao
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessengerRepositoryImpl(
    private val messageRemoteStorage: MessageRemoteStorage,
    private val messageFilesStorage: MessageFilesStorage,
    private val messageDao: MessageDao,
) : MessengerRepository {
    override suspend fun sendMessage(message: MyMessageEntity) {
        messageRemoteStorage.sendMessage(message = message)
        messageDao.insertMessage(message = message)
    }

    override suspend fun insertLocalMessages(messages: List<MyMessageEntity>) =
        messageDao.insertMessages(messages = messages)

    override suspend fun insertLocalMessage(message: MyMessageEntity) = messageDao.insertMessage(message = message)
    override suspend fun updateLocalMessage(newMessage: MyMessageEntity) = messageDao.updateMessage(message = newMessage)
    override fun getLocalMessageUpdate(): Flow<List<MyMessageEntity>> = messageDao.getAllMessagesLiveData()
    override fun getRemoteMessagesUpdate(): Flow<List<MyMessageEntity>> = messageRemoteStorage.getMessageUpdate()
    override suspend fun deleteLocalMessage(message: MyMessageEntity) = messageDao.deleteMessage(message)
    override fun deleteRemoteMessage(message: MyMessageEntity) = messageRemoteStorage.deleteMessage(message)
    override fun getNewMessageCount(): Flow<Int> = messageDao.getUnreadMessagesCount()

    override suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri> = withContext(Dispatchers.IO) {
        messageFilesStorage.uploadFile(uri, fileName)
    }

    override suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean> =
        messageFilesStorage.downloadFile(context, uri, fileName)

    override fun getUsersProfilesUpdate(): Flow<List<UserDataModel>> = messageRemoteStorage.getProfilesInMessenger()
    override suspend fun setMessageRead(id: Long) {
        messageDao.markMessageAsRead(id)
    }
}