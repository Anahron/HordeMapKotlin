package ru.newlevel.hordemap.data.repository

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
}