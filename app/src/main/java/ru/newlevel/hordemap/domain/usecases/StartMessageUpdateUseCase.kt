package ru.newlevel.hordemap.domain.usecases

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StartMessageUpdateUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(): MutableLiveData<List<MessageDataModel>> {
       return messengerRepository.startMessageUpdate()
    }
    fun getDownloadProgress(): MutableLiveData<Int> {
        return messengerRepository.getDownloadProgress()
    }
}