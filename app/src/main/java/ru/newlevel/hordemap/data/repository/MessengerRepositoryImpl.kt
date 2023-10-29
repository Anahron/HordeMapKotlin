package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.MessageStorage
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessengerRepositoryImpl(private val messageStorage: MessageStorage) : MessengerRepository {
    override fun sendMessage(text: String) {
        messageStorage.sendMessage(text = text)
    }

    override fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        return messageStorage.startMessageUpdate()
    }

    override fun stopMessageUpdate() {
        messageStorage.stopMessageUpdate()
    }

    override fun sendFile(uri: Uri, fileName: String?, fileSize: Long) {
       messageStorage.sendFile(uri, fileName, fileSize)
    }

    override fun downloadFile(context: Context, uri: Uri, fileName: String?) {
       messageStorage.downloadFile(context, uri, fileName)
    }

}