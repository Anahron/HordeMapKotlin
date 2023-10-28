package ru.newlevel.hordemap.presentatin.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.usecases.SendMessageUseCase
import ru.newlevel.hordemap.domain.usecases.StartMessageUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.StopMessageUpdateUseCase

class MessengerViewModel(
    private val stopMessageUpdateUseCase: StopMessageUpdateUseCase,
    private val startMessageUpdateUseCase: StartMessageUpdateUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private var messagesMutableLiveData = MutableLiveData<List<MessageDataModel>>()
    val messagesLiveData get(): LiveData<List<MessageDataModel>> = messagesMutableLiveData

    fun startMessageUpdate() {
        messagesMutableLiveData = startMessageUpdateUseCase.execute()
    }

    fun stopMessageUpdate() {
        stopMessageUpdateUseCase.execute()
    }

    fun sendMessage(text: String) {
        sendMessageUseCase.execute(text)
    }

    fun uploadFile(uri: Uri) {

    }
}