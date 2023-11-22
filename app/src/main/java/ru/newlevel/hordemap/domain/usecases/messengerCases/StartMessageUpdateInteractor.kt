package ru.newlevel.hordemap.domain.usecases.messengerCases

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StartMessageUpdateInteractor(private val messengerRepository: MessengerRepository) {
    fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
       return messengerRepository.getMessageUpdate()
    }
    fun getDownloadProgress(): MutableLiveData<Int> {
        return messengerRepository.getDownloadProgress()
    }
}