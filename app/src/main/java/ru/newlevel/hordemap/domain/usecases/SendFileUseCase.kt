package ru.newlevel.hordemap.domain.usecases

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class SendFileUseCase(private val messengerRepository: MessengerRepository) {
    fun execute(uri: Uri, fileName: String?, fileSize: Long){
      messengerRepository.sendFile(uri, fileName, fileSize)
    }
}