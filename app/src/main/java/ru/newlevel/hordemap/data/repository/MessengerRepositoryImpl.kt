package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.data.db.MessageDao
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessengerRepositoryImpl(
    private val messageRemoteStorage: MessageRemoteStorage,
    private val messageFilesStorage: MessageFilesStorage,
    private val messageDao: MessageDao
) : MessengerRepository {
    override suspend fun sendMessage(message: MyMessageEntity) {
        messageRemoteStorage.sendMessage(message = message)
        messageDao.insertMessage(message = message)
    }

    private val messages: LiveData<List<MyMessageEntity>> = messageDao.getAllMessagesLiveData()
    override fun getLocalMessageUpdate(): LiveData<List<MyMessageEntity>> = messages

    override suspend fun startMessageUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            messageRemoteStorage.getMessageUpdate().observeForever { firebaseMessages ->
                for (firebaseMessage in firebaseMessages) {
                    val existingRoomMessage = messages.value?.find { it.timestamp == firebaseMessage.timestamp }
                    if (existingRoomMessage != null) {
                        if (firebaseMessage.userName != existingRoomMessage.userName || firebaseMessage.selectedMarker != existingRoomMessage.selectedMarker || firebaseMessage.profileImageUrl != existingRoomMessage.profileImageUrl)
                            CoroutineScope(Dispatchers.IO).launch {
                                messageDao.updateMessage(
                                    existingRoomMessage.copy(
                                        userName = firebaseMessage.userName,
                                        selectedMarker = firebaseMessage.selectedMarker,
                                        profileImageUrl = firebaseMessage.profileImageUrl
                                    )
                                )
                            }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            messageDao.insertMessage(firebaseMessage)
                        }
                    }
                }
                if (messages.value != null)
                    for (roomMessage in messages.value!!) {
                        if (firebaseMessages.none { it.timestamp == roomMessage.timestamp }) {
                            CoroutineScope(Dispatchers.IO).launch {
                                messageDao.deleteMessage(roomMessage)
                            }
                        }
                    }
            }
        }
    }
    override fun stopMessageUpdate() {
        messageRemoteStorage.stopMessageUpdate()
    }

    override suspend fun uploadFile(uri: Uri, fileName: String?): String =
        messageFilesStorage.uploadFile(uri, fileName)


    override fun downloadFile(context: Context, uri: Uri, fileName: String?) =
        messageFilesStorage.downloadFile(context, uri, fileName)


    override fun getDownloadProgress(): MutableLiveData<Int> = messageFilesStorage.getDownloadProgress()


    override fun getUsersProfiles(): MutableLiveData<List<UserDataModel>> = messageRemoteStorage.getProfilesInMessenger()
}