package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessengerRepositoryImpl(private val messageRemoteStorage: MessageRemoteStorage, private val messageFilesStorage: MessageFilesStorage) : MessengerRepository {
    override fun sendMessage(text: String) {
        messageRemoteStorage.sendMessage(text = text)
    }

    override fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String) {
        messageRemoteStorage.sendMessage(text, downloadUrl, fileSize, fileName)
    }

    override fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        return messageRemoteStorage.startMessageUpdate()
    }

    override fun stopMessageUpdate() {
        messageRemoteStorage.stopMessageUpdate()
    }

    override suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): String {
        return messageFilesStorage.sendFile(message, uri, fileName, fileSize)
    }

    override fun downloadFile(context: Context, uri: Uri, fileName: String?) {
         messageFilesStorage.downloadFile(context, uri, fileName)
    }

    override fun getDownloadProgress(): MutableLiveData<Int> {
       return messageFilesStorage.getDownloadProgress()
    }

}