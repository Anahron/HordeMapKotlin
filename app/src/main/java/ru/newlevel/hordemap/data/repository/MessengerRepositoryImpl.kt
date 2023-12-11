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
import ru.newlevel.hordemap.data.storage.interfaces.MessagesCountLocalStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessengerRepositoryImpl(
    private val messageRemoteStorage: MessageRemoteStorage,
    private val messageFilesStorage: MessageFilesStorage,
    private val messageDao: MessageDao,
    private val messagesCountLocalStorage: MessagesCountLocalStorage
) : MessengerRepository {
    override suspend fun sendMessage(message: MyMessageEntity) {
        messageRemoteStorage.sendMessage(message = message)
        messageDao.insertMessage(message = message)
    }

    override suspend fun insertLocalMessages(messages: List<MyMessageEntity>) = messageDao.insertMessages(messages = messages)

    override suspend fun insertLocalMessage(message: MyMessageEntity) = messageDao.insertMessage(message = message)

    override suspend fun updateLocalMessage(newMessage: MyMessageEntity) = messageDao.updateMessage(message = newMessage)

    // private val messages: LiveData<List<MyMessageEntity>> = messageDao.getAllMessagesLiveData()

//    private var observer: Observer<in List<MyMessageEntity>> = Observer { firebaseMessages ->
//        for (firebaseMessage in firebaseMessages) {
//            val existingRoomMessage = messages.value?.find { it.timestamp == firebaseMessage.timestamp }
//            if (existingRoomMessage != null) {
//                if (firebaseMessage.userName != existingRoomMessage.userName || firebaseMessage.selectedMarker != existingRoomMessage.selectedMarker || firebaseMessage.profileImageUrl != existingRoomMessage.profileImageUrl || firebaseMessage.message != existingRoomMessage.message) {
//                    val newMessage = existingRoomMessage.copy(
//                        userName = firebaseMessage.userName,
//                        message = firebaseMessage.message,
//                        selectedMarker = firebaseMessage.selectedMarker,
//                        profileImageUrl = firebaseMessage.profileImageUrl
//                    )
//                    CoroutineScope(Dispatchers.IO).launch {
//                        messageDao.updateMessage(newMessage)
//                    }
//                }
//            } else CoroutineScope(Dispatchers.IO).launch {
//                messageDao.insertMessage(firebaseMessage)
//            }
//        }
//        if (messages.value != null)
//            for (roomMessage in messages.value!!) {
//                if (firebaseMessages.none { it.timestamp == roomMessage.timestamp }) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        messageDao.deleteMessage(roomMessage)
//                    }
//                }
//            }
//    }

    override fun getLocalMessageUpdate(): Flow<List<MyMessageEntity>> = messageDao.getAllMessagesLiveData()
    override fun getRemoteMessagesUpdate(): Flow<List<MyMessageEntity>> = messageRemoteStorage.getMessageUpdate()
    override suspend fun deleteLocalMessage(message: MyMessageEntity) = messageDao.deleteMessage(message)

    override fun deleteRemoteMessage(message: MyMessageEntity) = messageRemoteStorage.deleteMessage(message)


    override fun getNewMessageCount(): Flow<Int> = messagesCountLocalStorage.getNewMessageCount()


    override fun incrementNewMessageCount(increment: Int) = messagesCountLocalStorage.incrementNewMessageCount(increment)


    override suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri> = withContext(Dispatchers.IO) {
        messageFilesStorage.uploadFile(uri, fileName)
    }

    override suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean> =
        messageFilesStorage.downloadFile(context, uri, fileName)

    override fun getUsersProfilesUpdate(): Flow<List<UserDataModel>> = messageRemoteStorage.getProfilesInMessenger()
}